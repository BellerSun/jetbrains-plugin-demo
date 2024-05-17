package com.example.demo.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class MyActionTriggerGutterClear extends AnAction {

    private static final Logger logger = Logger.getInstance(MyActionTriggerGutterClear.class);

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


    }

}
