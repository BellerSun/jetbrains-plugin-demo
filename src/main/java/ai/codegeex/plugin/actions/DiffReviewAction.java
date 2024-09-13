package ai.codegeex.plugin.actions;

import com.intellij.diff.actions.impl.MutableDiffRequestChain;
import com.intellij.diff.chains.DiffRequestProducer;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.EmptyContent;
import com.intellij.diff.contents.FileDocumentContentImpl;
import com.intellij.diff.editor.ChainDiffVirtualFile;
import com.intellij.diff.impl.DiffRequestProcessor;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.ListSelection;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class DiffReviewAction extends AnAction{

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        if (anActionEvent == null) {
            throw new IllegalArgumentException("Argument for @NotNull parameter 'anActionEvent' of DiffReviewAction.update must not be null");
        }

        if (CodegeexApplicationSettings.settings().useOpenAIAPI) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] selectedFiles = fileEditorManager.getSelectedFiles();

        if (selectedFiles.length == 0 || !(selectedFiles[0] instanceof ChainDiffVirtualFile)) {
            return;
        }

        ChainDiffVirtualFile chainDiffVirtualFile = (ChainDiffVirtualFile) selectedFiles[0];
        SimpleDiffRequestChain simpleDiffRequestChain = getSimpleDiffRequestChain(chainDiffVirtualFile);

        if (simpleDiffRequestChain == null) {
            return;
        }

        DiffRequestProducer diffRequestProducer = getDiffRequestProducer(simpleDiffRequestChain);
        if (diffRequestProducer == null) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }

        VcsPrompting vcsPrompting = ServiceManager.getService(anActionEvent.getProject(), VcsPrompting.class);
        boolean hasPrompting = vcsPrompting != null && !vcsPrompting.a().isEmpty();

        anActionEvent.getPresentation().setIcon(CodegeexIcons.CODEGEEX);
        anActionEvent.getPresentation().setEnabled(hasPrompting);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (anActionEvent == null) {
            throw new IllegalArgumentException("Argument for @NotNull parameter 'anActionEvent' of DiffReviewAction.actionPerformed must not be null");
        }

        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }

        Editor editor = null;
        p_0 p_02 = p_0.a();
        String sessionId = CodegeexApplicationSettings.settings().sessionId;
        if (sessionId.isEmpty()) {
            p_02.a(project, true);
        }

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] selectedFiles = fileEditorManager.getSelectedFiles();

        if (selectedFiles.length == 0) {
            return;
        }

        try {
            if (selectedFiles[0] instanceof ChainDiffVirtualFile) {
                handleChainDiffVirtualFile(project, editor, (ChainDiffVirtualFile) selectedFiles[0]);
            } else if (selectedFiles[0] instanceof PreviewDiffVirtualFile) {
                handlePreviewDiffVirtualFile(project, editor, (PreviewDiffVirtualFile) selectedFiles[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SimpleDiffRequestChain getSimpleDiffRequestChain(ChainDiffVirtualFile chainDiffVirtualFile) {
        if (chainDiffVirtualFile.getChain() instanceof SimpleDiffRequestChain) {
            return (SimpleDiffRequestChain) chainDiffVirtualFile.getChain();
        }
        return null;
    }

    private DiffRequestProducer getDiffRequestProducer(SimpleDiffRequestChain simpleDiffRequestChain) {
        try {
            Method method = simpleDiffRequestChain.getClass().getMethod("getListSelection");
            ListSelection listSelection = (ListSelection) method.invoke(simpleDiffRequestChain);
            return (DiffRequestProducer) listSelection.getList().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private void handleChainDiffVirtualFile(Project project, Editor editor, ChainDiffVirtualFile chainDiffVirtualFile) throws Exception {
        MutableDiffRequestChain mutableDiffRequestChain = chainDiffVirtualFile.getChain() instanceof MutableDiffRequestChain ? (MutableDiffRequestChain) chainDiffVirtualFile.getChain() : null;
        SimpleDiffRequestChain simpleDiffRequestChain = chainDiffVirtualFile.getChain() instanceof SimpleDiffRequestChain ? (SimpleDiffRequestChain) chainDiffVirtualFile.getChain() : null;

        Document document1 = null;
        Document document2 = null;
        String fileName1 = null;
        String fileName2 = null;

        if (mutableDiffRequestChain != null) {
            FileDocumentContentImpl content1 = (FileDocumentContentImpl) mutableDiffRequestChain.getContent1();
            document1 = content1.getDocument();
            fileName1 = content1.getFile().getName();

            FileDocumentContentImpl content2 = (FileDocumentContentImpl) mutableDiffRequestChain.getContent2();
            document2 = content2.getDocument();
            fileName2 = content2.getFile().getName();
        } else if (simpleDiffRequestChain != null) {
            DiffRequestProducer diffRequestProducer = getDiffRequestProducer(simpleDiffRequestChain);
            if (diffRequestProducer == null) {
                return;
            }

            FutureTask<DiffRequest> futureTask = new FutureTask<>(() -> diffRequestProducer.process(simpleDiffRequestChain, new EmptyProgressIndicator()));
            ApplicationManager.getApplication().executeOnPooledThread(futureTask);
            DiffRequest diffRequest = futureTask.get();

            if (diffRequest instanceof ContentDiffRequest) {
                ContentDiffRequest contentDiffRequest = (ContentDiffRequest) diffRequest;
                List<DiffContent> contents = contentDiffRequest.getContents();

                if (contents.size() == 2) {
                    DocumentContent documentContent1 = (DocumentContent) contents.get(0);
                    document1 = documentContent1.getDocument();
                    fileName1 = documentContent1.getFile().getName();

                    DocumentContent documentContent2 = (DocumentContent) contents.get(1);
                    document2 = documentContent2.getDocument();
                    fileName2 = documentContent2.getFile().getName();
                }
            }
        }

        if (document1 != null && document2 != null) {
            createAndSendDiff(project, editor, document1, document2, fileName1, fileName2);
        }
    }

    private void handlePreviewDiffVirtualFile(Project project, Editor editor, PreviewDiffVirtualFile previewDiffVirtualFile) throws Exception {
        DiffRequestProcessor diffRequestProcessor = previewDiffVirtualFile.createProcessor(project);
        DiffRequest diffRequest = diffRequestProcessor.getActiveRequest();

        if (diffRequest == null) {
            return;
        }

        List<VirtualFile> filesToRefresh = getFilesToRefresh(diffRequest);

        if (filesToRefresh == null || filesToRefresh.isEmpty()) {
            handleSimpleDiffRequest(project, editor, (SimpleDiffRequest) diffRequest);
        } else {
            handleVcsHistory(project, editor, filesToRefresh.get(0));
        }
    }

    private List<VirtualFile> getFilesToRefresh(DiffRequest diffRequest) {
        try {
            Method method = diffRequest.getClass().getMethod("getFilesToRefresh");
            return (List<VirtualFile>) method.invoke(diffRequest);
        } catch (Exception e) {
            return null;
        }
    }

    private void handleSimpleDiffRequest(Project project, Editor editor, SimpleDiffRequest simpleDiffRequest) {
        List<DiffContent> contents = simpleDiffRequest.getContents();

        if (contents.size() == 2 && contents.get(1) instanceof EmptyContent) {
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            ApplicationManager.getApplication().runReadAction(() -> {
                DiffContent diffContent = contents.get(0);
                DocumentContent documentContent = (DocumentContent) diffContent;
                Document document = documentContent.getDocument();
                VirtualFile virtualFile = fileDocumentManager.getFile(document);
                if (virtualFile != null) {
                    createAndSendDiffFromDocument(project, editor, document, virtualFile.getName(), true);
                }
            });
        }
    }

    private void handleVcsHistory(Project project, Editor editor, VirtualFile virtualFile) {
        ProjectLevelVcsManager projectLevelVcsManager = ProjectLevelVcsManager.getInstance(project);
        AbstractVcs vcs = projectLevelVcsManager.getVcsFor(virtualFile);

        if (vcs == null) {
            return;
        }

        VcsHistoryProvider historyProvider = vcs.getVcsHistoryProvider();
        if (historyProvider == null) {
            return;
        }

        FilePath filePath = VcsUtil.getFilePath(virtualFile.getPath());
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                VcsHistorySession historySession = historyProvider.createSessionFor(filePath);
                if (historySession == null) {
                    createAndSendDiffFromDocument(project, editor, FileDocumentManager.getInstance().getDocument(virtualFile), virtualFile.getName(), false);
                    return;
                }

                List<VcsFileRevision> revisions = historySession.getRevisionList();
                if (revisions.isEmpty()) {
                    createAndSendDiffFromDocument(project, editor, FileDocumentManager.getInstance().getDocument(virtualFile), virtualFile.getName(), false);
                    return;
                }

                VcsFileRevision latestRevision = revisions.get(0);
                String originalContent = new String(latestRevision.loadContent(), StandardCharsets.UTF_8);
                String currentContent = VfsUtil.loadText(virtualFile);

                createAndSendDiff(project, editor, originalContent, currentContent, latestRevision.getRevisionNumber().toString(), virtualFile.getName());
            } catch (VcsException e) {
                e.printStackTrace();
            }
        });
    }

    private void createAndSendDiff(Project project, Editor editor, Document document1, Document document2, String fileName1, String fileName2) {
        List<String> originalLines = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document1.getText()));
        List<String> revisedLines = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document2.getText()));
        createAndSendDiff(project, editor, originalLines, revisedLines, fileName1, fileName2);
    }

    private void createAndSendDiff(Project project, Editor editor, String originalContent, String revisedContent, String fileName1, String fileName2) {
        List<String> originalLines = Arrays.asList(StringUtil.splitByLinesKeepSeparators(originalContent));
        List<String> revisedLines = Arrays.asList(StringUtil.splitByLinesKeepSeparators(revisedContent));
        createAndSendDiff(project, editor, originalLines, revisedLines, fileName1, fileName2);
    }

    private void createAndSendDiff(Project project, Editor editor, List<String> originalLines, List<String> revisedLines, String fileName1, String fileName2) {
        Patch<String> patch = DiffUtils.diff(originalLines, revisedLines);
        List<String> diff = UnifiedDiffUtils.generateUnifiedDiff(fileName1, fileName2, originalLines, patch, 0);
        String diffText = String.join("", diff);
        cU.a().a(project, editor, cM.g, true, diffText);
    }

    private void createAndSendDiffFromDocument(Project project, Editor editor, Document document, String fileName, boolean isCurrent) {
        List<String> lines = Arrays.asList(StringUtil.splitByLinesKeepSeparators(document.getText()));
        StringBuilder diffText = new StringBuilder("@@@ " + fileName + "\n");
        for (String line : lines) {
            diffText.append(isCurrent ? "+ " : "- ").append(line);
        }
        ApplicationManager.getApplication().invokeLater(() -> cU.a().a(project, editor, cM.g, true, diffText.toString()));
    }

    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
