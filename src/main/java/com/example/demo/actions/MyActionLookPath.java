package com.example.demo.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

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
            System.out.println("Path from PsiFile: " + pathFromPsiFile);
            logger.info("Path from PsiFile: " + pathFromPsiFile);
        } else {
            System.out.println("PsiFile is null");
            logger.info("PsiFile is null");
        }

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

}
