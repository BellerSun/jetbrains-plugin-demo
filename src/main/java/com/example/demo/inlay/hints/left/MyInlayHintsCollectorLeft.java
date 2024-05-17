package com.example.demo.inlay.hints.left;

import com.intellij.codeInsight.hints.InlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MyInlayHintsCollectorLeft implements InlayHintsCollector {

    private final Editor editor;
    private final InlayHintsSink sink;

    public MyInlayHintsCollectorLeft(Editor editor, InlayHintsSink sink) {
        this.editor = editor;
        this.sink = sink;
    }

    final Key<List<RangeHighlighterEx>> myInlayHintsCollectorLeft = Key.create("MyInlayHintsCollectorLeft");

    @Override
    public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
        PsiElement[] methods = PsiTreeUtil.getChildrenOfType(psiElement, PsiElement.class);
        if (methods != null) {
            List<RangeHighlighterEx> highlighterExes = editor.getUserData(myInlayHintsCollectorLeft);
            if (null == highlighterExes) {
                highlighterExes = new ArrayList<>();
                editor.putUserData(myInlayHintsCollectorLeft, highlighterExes);
            }

            MarkupModel markupModel = editor.getMarkupModel();
            for (RangeHighlighterEx highlighterEx : highlighterExes) {
                markupModel.removeHighlighter(highlighterEx);
            }
            highlighterExes.clear();


            for (PsiElement method : methods) {
                addGutterIconForMethod(method, highlighterExes);
            }
        }
        return true;
    }


    /**
     * debug那个Icon
     *
     * @param method
     */
    private void addGutterIconForMethod(PsiElement method, List<RangeHighlighterEx> highlighterExesEditor) {
        int offset = method.getTextOffset();
        int lineNumber = editor.getDocument().getLineNumber(offset);

        MarkupModel markupModel = editor.getMarkupModel();
        final RangeHighlighter[] highlighterExes = markupModel.getAllHighlighters();
        for (RangeHighlighter existingHighlighter : highlighterExes) {
            if (editor.getDocument().getLineNumber(existingHighlighter.getStartOffset()) == lineNumber) {
                // There is already a highlighter at this line, so we don't add a new one
                return;
            }
        }


        RangeHighlighter highlighter = markupModel.addLineHighlighter(lineNumber, 0, new TextAttributes());
        if (!(highlighter instanceof RangeHighlighterEx)) {
            return;
        }


        highlighterExesEditor.add((RangeHighlighterEx) highlighter);
        highlighter.setGutterIconRenderer(new GutterIconRenderer() {
            @NotNull
            @Override
            public Icon getIcon() {
                return MyIcons.logo;
            }

            @Override
            public String getTooltipText() {
                return "This is a hint for method: " + method.getText();
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }


            @Override
            public ActionGroup getPopupMenuActions() {
                return new ActionGroup() {
                    @Override
                    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent anActionEvent) {
                        final List<AnAction> list = new ArrayList<>();
                        list.add(new AnAction() {
                            // 实例初始化块
                            {
                                Presentation presentation = getTemplatePresentation();
                                presentation.setText("Bug修复");
                            }

                            @Override
                            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                                System.out.println("actionPerformed" + this.getTemplateText());
                            }
                        });
                        list.add(new AnAction() {
                            // 实例初始化块
                            {
                                Presentation presentation = getTemplatePresentation();
                                presentation.setText("代码解释");
                            }

                            @Override
                            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                                System.out.println("actionPerformed" + this.getTemplateText());
                            }
                        });
                        return list.toArray(new AnAction[0]);
                    }
                };
            }
        });
    }


}