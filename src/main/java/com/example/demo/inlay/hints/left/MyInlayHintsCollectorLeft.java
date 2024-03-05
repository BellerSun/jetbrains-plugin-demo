package com.example.demo.inlay.hints.left;

import com.intellij.codeInsight.hints.InlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;

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
        PsiMethod[] methods = PsiTreeUtil.getChildrenOfType(psiElement, PsiMethod.class);
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


            for (PsiMethod method : methods) {
                addGutterIconForMethod(method,highlighterExes);
            }
        }
        return true;
    }


    /**
     * debug那个Icon
     *
     * @param method
     */
    private void addGutterIconForMethod(PsiMethod method,List<RangeHighlighterEx> highlighterExesEditor) {
        int offset = method.getTextOffset();
        int lineNumber = editor.getDocument().getLineNumber(offset);

        MarkupModel markupModel = editor.getMarkupModel();
        final  RangeHighlighter[] highlighterExes= markupModel.getAllHighlighters();
        for (RangeHighlighter existingHighlighter : highlighterExes) {
            if (editor.getDocument().getLineNumber(existingHighlighter.getStartOffset()) == lineNumber) {
                // There is already a highlighter at this line, so we don't add a new one
                return;
            }
        }


        RangeHighlighter highlighter = markupModel.addLineHighlighter(lineNumber, 0, new TextAttributes());
        if (highlighter instanceof RangeHighlighterEx) {
            highlighterExesEditor.add((RangeHighlighterEx) highlighter);
            highlighter.setGutterIconRenderer(new GutterIconRenderer() {
                @NotNull
                @Override
                public Icon getIcon() {
                    return MyIcons.logo;
                }

                @Override
                public String getTooltipText() {
                    return "This is a hint for method: " + method.getName();
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
        }
    }


}