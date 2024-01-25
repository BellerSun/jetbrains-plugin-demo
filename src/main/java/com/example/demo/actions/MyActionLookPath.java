package com.example.demo.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.nnthink.relevant.FileScore;
import com.nnthink.relevant.FileSystem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MyActionLookPath extends AnAction {

    private static final Logger logger = Logger.getInstance(MyActionLookPath.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取Editor
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Document document = editor.getDocument();

        // Test getPath methods and print results
        String pathFromDocument = getPath(document);
        System.out.println("Path from Document: " + pathFromDocument);
        logger.info("Path from Document: " + pathFromDocument);

        String pathFromEditor = getPath(editor);
        System.out.println("Path from Editor: " + pathFromEditor);
        logger.info("Path from Editor: " + pathFromEditor);

        // To get PsiFile from Document
        PsiFile psiFile = PsiDocumentManager.getInstance(e.getProject()).getPsiFile(document);
        if (psiFile != null) {
            String pathFromPsiFile = getPath(psiFile);
            logger.info("Path from PsiFile: " + pathFromPsiFile);
        } else {
            logger.info("PsiFile is null");
        }


        this.computeFileSystem(e.getProject());
    }


    /**
     * 多方法从重载，计算文件路径
     */
    static String getPath(Document document) {
        final VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        assert vf != null;
        return vf.getPath();
    }

    static String getPath(PsiFile psiFile) {
        final VirtualFile vf = psiFile.getVirtualFile();
        return vf.getPath();
    }

    static String getPath(Editor editor) {
        final VirtualFile vf = FileDocumentManager.getInstance().getFile(editor.getDocument());
        return Optional.ofNullable(vf).map(VirtualFile::getPath).orElse(null);
    }


    private void computeFileSystem(Project project) {
        final List<PsiFile> allFiles = this.getAllFiles(project);

        final FileSystem fsNew = new FileSystem();
        String lastFile = "";
        for (PsiFile file : allFiles) {
            final VirtualFile vf = file.getVirtualFile();
            final String path = vf.getPath();
            logger.info("computeFileSystem path:" + path);
            fsNew.addSingleFile(path);
            lastFile = path;
        }

        List<FileScore> minN = fsNew.findMinN(lastFile, 5);
        for (FileScore fileScore : minN) {
            logger.info("fileScore path:" + fileScore.getFile().getPath());
        }
    }

    private List<PsiFile> getAllFiles(Project project) {
        final List<PsiFile> allFiles = new ArrayList<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            ProjectFileIndex.getInstance(project).iterateContent(fileOrDir -> {
                final String path = fileOrDir.getPath();
                if (path.contains("/.")) {
                    return true;
                }
                if (fileOrDir.isDirectory()) {
                    return true;
                }

                final PsiFile psiFile = PsiManager.getInstance(project).findFile(fileOrDir);
                if (psiFile == null) {
                    return true;
                }
                allFiles.add(psiFile);
                return true;
            });
        });
        return allFiles;
    }
}
