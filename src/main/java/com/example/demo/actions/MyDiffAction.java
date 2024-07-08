package com.example.demo.actions;

import com.github.difflib.DiffUtils;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsHistorySession;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.intellij.diff.actions.impl.MutableDiffRequestChain;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.DiffRequestProducer;
import com.intellij.diff.chains.DiffRequestProducerException;
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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationInfo;
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
import com.intellij.openapi.vcs.changes.PreviewDiffVirtualFile;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class MyDiffAction extends AnAction {

    private static final Logger logger = Logger.getInstance(MyDiffAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        try {
            actionPerformed0(anActionEvent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void actionPerformed0(@NotNull AnActionEvent anActionEvent) throws Exception {
        Project project = anActionEvent.getProject();

        ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
        String majorVersion = applicationInfo.getMajorVersion();


        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] arrvirtualFile = fileEditorManager.getSelectedFiles();

        boolean sw = System.getenv("aaa") != null;
        Method method;
        if (arrvirtualFile[0] instanceof ChainDiffVirtualFile) {
            FutureTask<DiffRequest> futureTask;
            Patch patch;
            ChainDiffVirtualFile chainDiffVirtualFile = (ChainDiffVirtualFile) arrvirtualFile[0];
            String string3 = null;
            String string4 = null;
            Document document = null;
            Document document2 = null;
            DiffRequestChain diffVirtualFileChain = chainDiffVirtualFile.getChain();
            boolean isMutable = diffVirtualFileChain instanceof MutableDiffRequestChain;
            boolean isSimple = diffVirtualFileChain instanceof SimpleDiffRequestChain;

            if (isMutable) {
                MutableDiffRequestChain mutableDiffRequestChain = (MutableDiffRequestChain) diffVirtualFileChain;
                FileDocumentContentImpl fileDocumentContentImpl = (FileDocumentContentImpl) mutableDiffRequestChain.getContent1();
                document = fileDocumentContentImpl.getDocument();
                string3 = fileDocumentContentImpl.getFile().getName();

                FileDocumentContentImpl content2 = (FileDocumentContentImpl) mutableDiffRequestChain.getContent2();

                document2 = content2.getDocument();
                string4 = content2.getFile().getName();
            } else if (isSimple) {
                SimpleDiffRequestChain simpleDiffRequestChain = (SimpleDiffRequestChain) diffVirtualFileChain;
                Class chainClass = simpleDiffRequestChain.getClass();
                Method getListSelection = chainClass.getMethod("getListSelection", new Class[0]);
                ListSelection listSelection = (ListSelection) getListSelection.invoke(simpleDiffRequestChain, new Object[0]);
                DiffRequestProducer diffRequestProducer = (DiffRequestProducer) listSelection.getList().get(0);
                futureTask = new FutureTask<>(new Callable<DiffRequest>() {
                    public DiffRequest a() throws DiffRequestProducerException {
                        return diffRequestProducer.process(simpleDiffRequestChain, new EmptyProgressIndicator());
                    }

                    @Override
                    public DiffRequest call() throws DiffRequestProducerException {
                        return this.a();
                    }
                });
                ApplicationManager.getApplication().executeOnPooledThread(futureTask);
                DiffRequest diffRequest = futureTask.get();
                if (!(diffRequest instanceof ContentDiffRequest)) return;
                ContentDiffRequest contentDiffRequest = (ContentDiffRequest) diffRequest;
                List list = contentDiffRequest.getContents();
                if (list.size() != 2) return;
                try {
                    DiffContent diffContent = (DiffContent) list.get(0);
                    DiffContent diffContent2 = (DiffContent) list.get(1);
                    DocumentContent documentContent = (DocumentContent) diffContent;
                    document = documentContent.getDocument();
                    FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
                    VirtualFile virtualFile = fileDocumentManager.getFile(document);
                    if (virtualFile != null) {
                        string3 = virtualFile.getName();
                    }
                    FileDocumentContentImpl fileDocumentContentImpl = (FileDocumentContentImpl) diffContent2;
                    document2 = fileDocumentContentImpl.getDocument();
                    string4 = fileDocumentContentImpl.getFile().getName();
                } catch (Exception exception) {
                    DiffContent diffContent = (DiffContent) list.get(0);
                    DiffContent diffContent3 = (DiffContent) list.get(1);
                    DocumentContent documentContent = (DocumentContent) diffContent;
                    document = documentContent.getDocument();
                    DocumentContent documentContent2 = (DocumentContent) diffContent3;
                    document2 = documentContent2.getDocument();
                    string3 = "Current/Clipboard";
                    string4 = "Generated";
                }
            }


            List<String> list1 = Arrays.asList(StringUtil.splitByLinesKeepSeparators((String) document.getText()));
            List<String> list = Arrays.asList(StringUtil.splitByLinesKeepSeparators((String) document2.getText()));
            Patch<String> patchStr = DiffUtils.diff(list1, list);
            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(string3, string4, list1, patchStr, (int) 0);
            String string5 = String.join("", unifiedDiff);
            //cU.a().a(project, editor, cM.g, true, string5);
            return;
        }


        if (!(arrvirtualFile[0] instanceof PreviewDiffVirtualFile)) return;


        PreviewDiffVirtualFile previewDiffVirtualFile = (PreviewDiffVirtualFile) arrvirtualFile[0];
        DiffRequestProcessor diffRequestProcessor = previewDiffVirtualFile.createProcessor(project);
        DiffRequest diffRequest = diffRequestProcessor.getActiveRequest();
        List list = null;

        method = diffRequest.getClass().

                getMethod("getFilesToRefresh", new Class[0]);

        list = (List) method.invoke((Object) diffRequest, new Object[0]);
        if (list == null || list.size() == 0) {
            SimpleDiffRequest simpleDiffRequest = (SimpleDiffRequest) diffRequest;
            List list2 = simpleDiffRequest.getContents();
            if (list2.size() != 2 || !(list2.get(1) instanceof EmptyContent)) return;
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            ApplicationManager.getApplication().runReadAction(() -> {
                DiffContent diffContent = (DiffContent) list2.get(0);
                DocumentContent documentContent = (DocumentContent) diffContent;
                Document document = documentContent.getDocument();
                VirtualFile virtualFile = fileDocumentManager.getFile(document);
                List<String> list3 = Arrays.asList(StringUtil.splitByLinesKeepSeparators((String) document.getText()));
                StringBuilder stringBuilder = new StringBuilder("@@@ " + virtualFile.getName() + "\n");
                for (String string : list3) {
                    stringBuilder.append("- ").append(string);
                }
                String string = stringBuilder.toString();
                //ApplicationManager.getApplication().invokeLater(() -> cU.a().a(project, null, cM.g, true, string));
            });
            return;
        }

        VirtualFile vf = (VirtualFile) list.get(0);
        ProjectLevelVcsManager projectLevelVcsManager = ProjectLevelVcsManager.getInstance((Project) project);
        AbstractVcs abstractVcs = projectLevelVcsManager.getVcsFor(vf);
        if (abstractVcs == null) {
            return;
        }

        VcsHistoryProvider vcsHistoryProvider = abstractVcs.getVcsHistoryProvider();
        if (vcsHistoryProvider == null) {
            return;
        }

        FilePath filePath = VcsUtil.getFilePath( vf.getPath());
        System.out.println();
        //Editor editor2 = editor;
        ApplicationManager.getApplication().executeOnPooledThread(() -> a(vcsHistoryProvider, filePath, vf, project));
        return;
    }



    private static void a(VcsHistoryProvider vcsHistoryProvider, FilePath filePath, VirtualFile virtualFile, Project project) {
        VcsHistorySession vcsHistorySession = null;
        try {
            vcsHistorySession = vcsHistoryProvider.createSessionFor(filePath);
        }
        catch (VcsException vcsException) {
        }
        if (vcsHistorySession == null) {
            return;
        }
        List list = vcsHistorySession.getRevisionList();
        try {
            if (list.isEmpty()) {
                FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
                ApplicationManager.getApplication().runReadAction(() -> {
                    Document document = fileDocumentManager.getDocument(virtualFile);
                    List<String> list2 = Arrays.asList(StringUtil.splitByLinesKeepSeparators((String)document.getText()));
                    StringBuilder stringBuilder = new StringBuilder("@@@ " + virtualFile.getName() + "\n");
                    for (String string : list2) {
                        stringBuilder.append("+ ").append(string);
                    }
                    String string = stringBuilder.toString();
                    System.out.println();
                    //ApplicationManager.getApplication().invokeLater(() -> cU.a().a(project, editor, cM.g, true, string));
                });
            } else {
                VcsFileRevision vcsFileRevision = (VcsFileRevision)list.get(0);
                byte[] arrby = vcsFileRevision.loadContent();
                String string = new String(arrby, StandardCharsets.UTF_8);
                String string2 = VfsUtil.loadText((VirtualFile)virtualFile);
                List<String> list2 = Arrays.asList(StringUtil.splitByLinesKeepSeparators((String)string));
                List<String> list3 = Arrays.asList(StringUtil.splitByLinesKeepSeparators((String)string2));
                Patch patch = DiffUtils.diff(list2, list3);
                List list4 = UnifiedDiffUtils.generateUnifiedDiff((String)vcsFileRevision.getRevisionNumber().toString(), (String)virtualFile.getName(), list2, (Patch)patch, (int)0);
                String string3 = String.join((CharSequence)"", list4);
                System.out.println();
                //ApplicationManager.getApplication().invokeLater(() -> cU.a().a(project, editor, cM.g, true, string3));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

