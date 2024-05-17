package com.example.demo.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.nnthink.relevant.FileScore;
import com.nnthink.relevant.FileSystem;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MyActionTriggerGutter extends AnAction {

    private static final Logger logger = Logger.getInstance(MyActionTriggerGutter.class);

    public static final Key<List> HIGHLIGHTER_EX_KEY = Key.create("HIGHLIGHTER_EX_KEY");


    private static int methodCnt = 0;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        // 获取Editor
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        MarkupModel markupModel = editor.getMarkupModel();
        List<RangeHighlighter> oldHL = editor.getUserData(MyActionTriggerGutter.HIGHLIGHTER_EX_KEY);
        if (null != oldHL) {
            for (RangeHighlighter rangeHighlighterEx : oldHL) {
                markupModel.removeHighlighter(rangeHighlighterEx);
            }
            editor.putUserData(MyActionTriggerGutter.HIGHLIGHTER_EX_KEY, null);
        }


        // 挨个+1换下一个方法
        PsiElement[] allMethod = getAllMethod(editor, project);
        if (methodCnt >= allMethod.length) {
            methodCnt = 0;
        }
        PsiMethod method = (PsiMethod) allMethod[methodCnt++];

        // 在该方法左侧，加上图标

        List<RangeHighlighter> newHL = new ArrayList<>();
        for (int i = 0; i < 10; i++) {

            newHL.add(addGutterIconForMethod(method, editor, project,i));
        }

        editor.putUserData(HIGHLIGHTER_EX_KEY, newHL);
    }


    private RangeHighlighter addGutterIconForMethod(PsiMethod method, Editor editor, Project project, int layer) {
        int offset = method.getTextOffset();
        int lineNumber = editor.getDocument().getLineNumber(offset);

        MarkupModel markupModel = editor.getMarkupModel();

        RangeHighlighter highlighter = markupModel.addLineHighlighter(lineNumber, layer, new TextAttributes());
        highlighter.setGutterIconRenderer(new GutterIconRenderer() {
            @NotNull
            @Override
            public Icon getIcon() {
                return MyIcons.stocking;
            }

            @Override
            public String getTooltipText() {
                return "嘿嘿嘿: " + method.getName();
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        });

        return highlighter;
    }

    private PsiElement[] getAllMethod(Editor editor, Project project) {
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile != null) {
            return PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class).toArray(PsiMethod[]::new);
        }
        return new PsiMethod[0];
    }
}
