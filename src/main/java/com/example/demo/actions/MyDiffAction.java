package com.example.demo.actions;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.intellij.diff.actions.impl.MutableDiffRequestChain;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.DiffRequestProducer;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.FileDocumentContentImpl;
import com.intellij.diff.editor.ChainDiffVirtualFile;
import com.intellij.diff.impl.DiffRequestProcessor;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.tools.util.DiffDataKeys;
import com.intellij.openapi.ListSelection;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.PreviewDiffVirtualFile;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.history.VcsHistorySession;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class MyDiffAction extends AnAction {

    private static final Logger logger = Logger.getInstance(MyDiffAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        try {
            processDiffAction(anActionEvent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理差异操作的主入口
     *
     * @param anActionEvent 动作事件
     */
    private void processDiffAction(@NotNull AnActionEvent anActionEvent) throws Exception {
        final Project project = anActionEvent.getProject();
        DiffRequest diffRequest = anActionEvent.getData(DiffDataKeys.DIFF_REQUEST);

        if (diffRequest instanceof ContentDiffRequest contentDiffRequest) {
            final List<DiffContent> contents = contentDiffRequest.getContents();
            if (contents.size() == 2) {
                DiffContent c1 = contents.get(0);
                DiffContent c2 = contents.get(1);
                handleDiffContent(project, c1, c2);
                return;
            } else {
                System.out.println("[MY_DIFF] Not a diff content request, contentSize:" + contents.size());
            }
        }


        final VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();

        // 如果没有选中文件，则返回
        if (selectedFiles.length == 0) return;

        final VirtualFile selectedFile = selectedFiles[0];
        if (selectedFile instanceof ChainDiffVirtualFile) {
            // 处理链式差异虚拟文件
            processChainDiffVirtualFile(project, (ChainDiffVirtualFile) selectedFile);
        } else if (selectedFile instanceof PreviewDiffVirtualFile) {
            // 处理预览差异虚拟文件
            processPreviewDiffVirtualFile(project, (PreviewDiffVirtualFile) selectedFile);
        } else {
            System.out.println("[MY_DIFF] Not a diff file, fileType" + selectedFile.getClass().getName());
        }
    }

    /**
     * 处理链式差异虚拟文件
     *
     * @param project              当前项目
     * @param chainDiffVirtualFile 链式差异虚拟文件
     */
    private void processChainDiffVirtualFile(Project project, ChainDiffVirtualFile chainDiffVirtualFile) throws Exception {
        // 获取差异请求链
        final DiffRequestChain diffRequestChain = chainDiffVirtualFile.getChain();

        if (diffRequestChain instanceof MutableDiffRequestChain) {
            //处理可变差异请求链
            processMutableDiffRequestChain(project, (MutableDiffRequestChain) diffRequestChain);
        } else if (diffRequestChain instanceof SimpleDiffRequestChain) {
            // 处理简单差异请求链
            processSimpleDiffRequestChain(project, (SimpleDiffRequestChain) diffRequestChain);
        } else {
            System.out.println("[MY_DIFF] Not a diff chain file, chainType:" + diffRequestChain.getClass().getName());
        }
    }

    /**
     * 处理可变差异请求链
     *
     * @param project 当前项目
     * @param chain   可变差异请求链
     */
    private void processMutableDiffRequestChain(Project project, MutableDiffRequestChain chain) {
        FileDocumentContentImpl content1 = (FileDocumentContentImpl) chain.getContent1();
        FileDocumentContentImpl content2 = (FileDocumentContentImpl) chain.getContent2();

        Document document1 = content1.getDocument();
        Document document2 = content2.getDocument();

        String fileName1 = content1.getFile().getName();
        String fileName2 = content2.getFile().getName();

        generateAndShowDiff(project, document1, document2, fileName1, fileName2);
    }

    /**
     * 处理简单差异请求链
     *
     * @param project 当前项目
     * @param chain   简单差异请求链
     */
    private void processSimpleDiffRequestChain(Project project, SimpleDiffRequestChain chain) throws Exception {
        ListSelection listSelection = (ListSelection) chain.getClass().getMethod("getListSelection").invoke(chain);
        DiffRequestProducer producer = (DiffRequestProducer) listSelection.getList().get(0);

        FutureTask<DiffRequest> futureTask = new FutureTask<>(() -> producer.process(chain, new EmptyProgressIndicator()));
        ApplicationManager.getApplication().executeOnPooledThread(futureTask);
        DiffRequest diffRequest = futureTask.get();

        if (diffRequest instanceof ContentDiffRequest) {
            ContentDiffRequest contentDiffRequest = (ContentDiffRequest) diffRequest;
            List<DiffContent> contents = contentDiffRequest.getContents();

            if (contents.size() == 2) {
                handleDiffContent(project, contents.get(0), contents.get(1));
            }
        }
    }

    /**
     * 处理差异内容
     *
     * @param project  当前项目
     * @param content1 第一个差异内容
     * @param content2 第二个差异内容
     */
    private void handleDiffContent(Project project, DiffContent content1, DiffContent content2) {
        try {
            Document document1 = ((DocumentContent) content1).getDocument();
            Document document2 = ((DocumentContent) content2).getDocument();
            VirtualFile file1 = FileDocumentManager.getInstance().getFile(document1);
            VirtualFile file2 = ((FileDocumentContentImpl) content2).getFile();

            String fileName1 = (file1 != null) ? file1.getName() : "Current/Clipboard";
            String fileName2 = file2.getName();

            generateAndShowDiff(project, document1, document2, fileName1, fileName2);
        } catch (Exception e) {
            Document document1 = ((DocumentContent) content1).getDocument();
            Document document2 = ((DocumentContent) content2).getDocument();

            generateAndShowDiff(project, document1, document2, "Current/Clipboard", "Generated");
        }
    }


    private int diffType = 0;

    /**
     * 生成并显示差异
     *
     * @param project   当前项目
     * @param document1 第一个文档
     * @param document2 第二个文档
     * @param fileName1 第一个文件名
     * @param fileName2 第二个文件名
     */
    private void generateAndShowDiff(Project project, Document document1, Document document2, String fileName1, String fileName2) {

        generateAndShowFullDiffUnified(project, document1, document2, fileName1, fileName2);
        //generateAndShowFullDiff(project, document1, document2, fileName1, fileName2);

        generateAndShowSimpleDiffUnified(project, document1, document2, fileName1, fileName2);
/*        int diffType = this.diffType % 3;
        switch (diffType) {
            case 0:
            case 1:
            case 2:
            default:
                System.out.println("[DIFF] diffType not support");
        }
        this.diffType++;*/
    }

    private void generateAndShowSimpleDiffUnified(Project project, Document document1, Document document2, String fileName1, String fileName2) {


        List<String> lines1 = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document1.getText()));
        List<String> lines2 = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document2.getText()));

        Patch<String> patch = DiffUtils.diff(lines1, lines2);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(fileName1, fileName2, lines1, patch, 0);

        List<String> unifiedDiffHas = unifiedDiff.stream().map(line -> line + (line.endsWith("\n") ? "" : "\n")).toList();
        String diffText = String.join("", unifiedDiffHas);
        System.out.println("[DIFF_SIMPLE_UNIFIED]------------------------------------------↓↓↓↓↓↓↓↓↓↓↓↓--------------------------------------------------");
        System.out.println(diffText);
        System.out.println("[DIFF_SIMPLE_UNIFIED]------------------------------------------↑↑↑↑↑↑↑↑↑↑↑↑--------------------------------------------------");
        // Show the diff using your preferred method
        // cU.a().a(project, editor, cM.g, true, diffText);
    }

    private void generateAndShowFullDiffUnified(Project project, Document document1, Document document2, String fileName1, String fileName2) {
        List<String> lines1 = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document1.getText()));
        List<String> lines2 = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document2.getText()));

        Patch<String> patch = DiffUtils.diff(lines1, lines2);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(fileName1, fileName2, lines1, patch, 10000);

        List<String> unifiedDiffHas = unifiedDiff.stream().map(line -> line + (line.endsWith("\n") ? "" : "\n")).toList();

        String diffText = String.join("", unifiedDiffHas);
        System.out.println("[DIFF_FULL_UNIFIED]------------------------------------------↓↓↓↓↓↓↓↓↓↓↓↓--------------------------------------------------");
        System.out.println(diffText);
        System.out.println("[DIFF_FULL_UNIFIED]------------------------------------------↑↑↑↑↑↑↑↑↑↑↑↑--------------------------------------------------");
    }


    private void generateAndShowFullDiff(Project project, Document document1, Document document2, String fileName1, String fileName2) {
        List<String> lines1 = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document1.getText()));
        List<String> lines2 = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document2.getText()));

        // 使用DiffRowGenerator来生成详细的diff行
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true) // 显示行内差异
                .inlineDiffByWord(true) // 逐词显示行内差异
                .build();
        List<DiffRow> rows = generator.generateDiffRows(lines1, lines2);

        // 将详细的diff行组合成完整的diff文本
        StringBuilder diffText = new StringBuilder();
        for (DiffRow row : rows) {
            diffText.append(row.getOldLine()).append(" | ").append(row.getNewLine()).append("\n");
        }

        System.out.println("[DIFF_FULL]------------------------------------------↓↓↓↓↓↓↓↓↓↓↓↓--------------------------------------------------");
        System.out.println(diffText);
        System.out.println("[DIFF_FULL]------------------------------------------↑↑↑↑↑↑↑↑↑↑↑↑--------------------------------------------------");
        // Show the diff using your preferred method
        // cU.a().a(project, editor, cM.g, true, diffText.toString());
    }

    /**
     * 处理预览差异虚拟文件
     *
     * @param project     当前项目
     * @param previewFile 预览差异虚拟文件
     */
    private void processPreviewDiffVirtualFile(Project project, PreviewDiffVirtualFile previewFile) throws Exception {
        DiffRequestProcessor processor = previewFile.createProcessor(project);
        DiffRequest diffRequest = processor.getActiveRequest();

        Method method = diffRequest.getClass().getMethod("getFilesToRefresh");
        List<VirtualFile> filesToRefresh = (List<VirtualFile>) method.invoke(diffRequest);

        if (filesToRefresh == null || filesToRefresh.isEmpty()) {
            handleSimpleDiffRequest(project, (SimpleDiffRequest) diffRequest);
        } else {
            refreshFiles(project, filesToRefresh.get(0));
        }
    }

    /**
     * 处理简单差异请求
     *
     * @param project 当前项目
     * @param request 简单差异请求
     */
    private void handleSimpleDiffRequest(Project project, SimpleDiffRequest request) {
        List<DiffContent> contents = request.getContents();
        if (contents.size() != 2) {
            return;
        }

        ApplicationManager.getApplication().runReadAction(() -> {

            handleDiffContent(project, contents.get(0), contents.get(1));


/*            Document document = ((DocumentContent) contents.get(0)).getDocument();
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            if (file==null){
                return;
            }
            List<String> lines = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document.getText()));
            StringBuilder diffBuilder = new StringBuilder("@@@ " + file.getName() + "\n");
            lines.forEach(line -> diffBuilder.append("- ").append(line));
            String diffText = diffBuilder.toString();
            System.out.println(diffText);*/
            // Show the diff using your preferred method
            // cU.a().a(project, null, cM.g, true, diffText);
        });
    }

    /**
     * 刷新文件
     *
     * @param project 当前项目
     * @param file    虚拟文件
     */
    private void refreshFiles(Project project, VirtualFile file) {
        ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
        AbstractVcs vcs = vcsManager.getVcsFor(file);

        if (vcs == null) return;

        VcsHistoryProvider historyProvider = vcs.getVcsHistoryProvider();
        if (historyProvider == null) return;

        FilePath filePath = VcsUtil.getFilePath(file.getPath());
        ApplicationManager.getApplication().executeOnPooledThread(() -> loadAndShowVcsHistory(historyProvider, filePath, file, project));
    }

    /**
     * 加载并显示版本控制系统历史记录
     *
     * @param provider 版本控制系统历史记录提供者
     * @param filePath 文件路径
     * @param file     虚拟文件
     * @param project  当前项目
     */
    private void loadAndShowVcsHistory(VcsHistoryProvider provider, FilePath filePath, VirtualFile file, Project project) {
        try {
            VcsHistorySession session = provider.createSessionFor(filePath);

            if (session == null || session.getRevisionList().isEmpty()) {
                showCurrentFileContent(project, file);
            } else {
                showVcsDiff(project, file, session.getRevisionList().get(0));
            }
        } catch (VcsException e) {
            logger.error("Failed to load VCS history", e);
        }
    }

    /**
     * 显示当前文件内容
     *
     * @param project 当前项目
     * @param file    虚拟文件
     */
    private void showCurrentFileContent(Project project, VirtualFile file) {
        FileDocumentManager manager = FileDocumentManager.getInstance();
        ApplicationManager.getApplication().runReadAction(() -> {
            Document document = manager.getDocument(file);
            List<String> lines = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document.getText()));
            StringBuilder diffBuilder = new StringBuilder("@@@ " + file.getName() + "\n");
            lines.forEach(line -> diffBuilder.append("+ ").append(line));

            String diffText = diffBuilder.toString();
            // Show the diff using your preferred method
            // cU.a().a(project, editor, cM.g, true, diffText);
        });
    }

    /**
     * 显示版本控制系统差异
     *
     * @param project  当前项目
     * @param file     虚拟文件
     * @param revision 版本文件修订
     */
    private void showVcsDiff(Project project, VirtualFile file, VcsFileRevision revision) {
        try {
            byte[] content = revision.loadContent();
            String oldText = new String(content, StandardCharsets.UTF_8);
            String newText = VfsUtil.loadText(file);

            List<String> oldLines = Arrays.asList(StringUtil.splitByLinesKeepSeparators(oldText));
            List<String> newLines = Arrays.asList(StringUtil.splitByLinesKeepSeparators(newText));

            Patch<String> patch = DiffUtils.diff(oldLines, newLines);
            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(revision.getRevisionNumber().toString(), file.getName(), oldLines, patch, 0);

            String diffText = String.join("", unifiedDiff);
            // Show the diff using your preferred method
            // cU.a().a(project, editor, cM.g, true, diffText);
        } catch (Exception e) {
            logger.error("Failed to show VCS diff", e);
        }
    }
}
