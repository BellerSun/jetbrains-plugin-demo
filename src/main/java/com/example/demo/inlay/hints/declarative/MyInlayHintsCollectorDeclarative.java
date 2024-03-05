package com.example.demo.inlay.hints.declarative;

import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.InlayPresentationFactory;
import com.intellij.codeInsight.hints.declarative.*;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import icons.MyIcons;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MyInlayHintsCollectorDeclarative implements SharedBypassCollector {

    private final Editor editor;
    private final PsiFile psiFile;

    public MyInlayHintsCollectorDeclarative(PsiFile psiFile, Editor editor) {
        this.editor = editor;
        this.psiFile = psiFile;
    }


    public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
        PsiMethod[] methods = PsiTreeUtil.getChildrenOfType(psiElement, PsiMethod.class);
        if (methods != null) {
            for (PsiMethod method : methods) {
                addMethodInlayTipForMethod(editor, method, inlayHintsSink);
            }
        }
        return true;
    }


    private void addMethodInlayTipForMethod(Editor editor, PsiMethod method, InlayHintsSink inlayHintsSink) {
        int textOffset = method.getTextOffset();
        Document document = editor.getDocument();
        int lineNumber = document.getLineNumber(textOffset);
        String text = document.getText(new TextRange(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber)));
        // 查看text左边有几个空格
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                //sb.append("   ");
            } else {
                break;
            }
        }


        InlayPresentation presentation = this.generateInlayPresentation(editor, sb.append("hintText").toString());
/*        int offset = method.getNameIdentifier().getTextRange().getStartOffset();
        inlayHintsSink.addInlineElement(offset, true, hintText);*/
        inlayHintsSink.addBlockElement(textOffset, true, true, 1, presentation);

    }

    private InlayPresentation generateInlayPresentation(Editor editor, String hintText) {

        InlayPresentation inlayPresentation = new PresentationFactory(editor).smallText(hintText);
        InlayPresentation inlayPresentation1 = new PresentationFactory(editor).referenceOnHover(inlayPresentation, new InlayPresentationFactory.ClickListener() {
            @Override
            public void onClick(@NotNull MouseEvent mouseEvent, @NotNull Point point) {
                System.out.println("click");
            }
        });

        return inlayPresentation1;
    }


    @Override
    public void collectFromElement(@NotNull PsiElement psiElement, @NotNull InlayTreeSink inlayTreeSink) {


        PsiMethod[] methods = PsiTreeUtil.getChildrenOfType(psiElement, PsiMethod.class);
        if (methods != null) {
            for (PsiMethod method : methods) {
                int textOffset = method.getTextOffset();

                InlineInlayPosition inlayPosition = new InlineInlayPosition(textOffset, true, 0);


                Function1<PresentationTreeBuilder, Unit> function1 = presentationTreeBuilder -> {
                    presentationTreeBuilder.text("hintTextDeclarative", null);
                    presentationTreeBuilder.clickHandlerScope(new InlayActionData(new StringInlayActionPayload("aaa"), "aaa"), presentationTreeBuilder1 -> Unit.INSTANCE);
                    return Unit.INSTANCE;
                };
                inlayTreeSink.addPresentation(inlayPosition, new ArrayList<>(), "hintDeclarative", true, function1);

            }
        }
    }

    @Override
    public void collectFromElementForActions(@NotNull PsiElement element, @NotNull InlayTreeSink sink) {
        SharedBypassCollector.super.collectFromElementForActions(element, sink);
    }
}