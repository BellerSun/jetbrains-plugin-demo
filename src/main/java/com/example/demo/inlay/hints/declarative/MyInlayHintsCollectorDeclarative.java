package com.example.demo.inlay.hints.declarative;

import com.intellij.codeInsight.hints.declarative.*;
import com.intellij.codeInsight.hints.declarative.impl.PresentationTreeBuilderImpl;
import com.intellij.codeInsight.hints.declarative.impl.util.TinyTree;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MyInlayHintsCollectorDeclarative implements SharedBypassCollector {

    private final Editor editor;
    private final PsiFile psiFile;

    public MyInlayHintsCollectorDeclarative(PsiFile psiFile, Editor editor) {
        this.editor = editor;
        this.psiFile = psiFile;
    }

    @SuppressWarnings("all")
    @Override
    public void collectFromElement(@NotNull PsiElement psiElement, @NotNull InlayTreeSink inlayTreeSink) {
        PsiElement[] methods = PsiTreeUtil.getChildrenOfType(psiElement, PsiElement.class);
        SmartPsiElementPointer<PsiElement> pointer = SmartPointerManager.getInstance(psiElement.getProject()).createSmartPsiElementPointer(psiElement);

        if (methods != null) {
            for (PsiElement method : methods) {
                int textOffset = method.getTextOffset();

                InlineInlayPosition inlayPosition = new InlineInlayPosition(textOffset, true, 0);
                EndOfLinePosition endOfLinePosition = new EndOfLinePosition(editor.getDocument().getLineNumber(textOffset));

                Function1<PresentationTreeBuilder, Unit> function1 = presentationTreeBuilder -> {
                    presentationTreeBuilder.text("hintTextDeclarative", null);
                    presentationTreeBuilder.clickHandlerScope(new InlayActionData(new PsiPointerInlayActionPayload(pointer), "aaa"), presentationTreeBuilder1 -> Unit.INSTANCE);
                    return Unit.INSTANCE;
                };


                inlayTreeSink.addPresentation(inlayPosition, new ArrayList<>(), "方法前1-tooltip", true, presentationTreeBuilder -> {
                    // presentationTreeBuilder.text("方法前1", null);
                    presentationTreeBuilder.clickHandlerScope(new InlayActionData(new PsiPointerInlayActionPayload(pointer), "方法前1"), ptb -> {

                        System.out.println("clickHandlerScope 方法前1");
                        ptb.text("方法前1", null);

                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                });
                inlayTreeSink.addPresentation(inlayPosition, new ArrayList<>(), "方法前2-tooltip", true, presentationTreeBuilder -> {

                    presentationTreeBuilder.clickHandlerScope(new InlayActionData(new PsiPointerInlayActionPayload(pointer), "方法前2"), ptb -> {
                        System.out.println("clickHandlerScope 方法前2");
                        ptb.text("方法前2", null);


                        if (ptb instanceof PresentationTreeBuilderImpl) {
                            PresentationTreeBuilderImpl ptbImpl = (PresentationTreeBuilderImpl) ptb;
                            TinyTree<Object> complete = ptbImpl.complete();
                            Object dataPayload = complete.getDataPayload(Byte.parseByte("1"));
                            if (dataPayload instanceof InlayActionData) {
                                InlayActionData pl = (InlayActionData) dataPayload;
                                InlayActionPayload payload = pl.getPayload();
                                if (payload instanceof PsiPointerInlayActionPayload) {
                                    PsiPointerInlayActionPayload psiPl = (PsiPointerInlayActionPayload) payload;
                                    PsiElement element = psiPl.getPointer().getElement();
                                    System.out.println("");
                                }
                            }
                        }

                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                });
                inlayTreeSink.addPresentation(endOfLinePosition, new ArrayList<>(), "方法后1-tooltip", true, presentationTreeBuilder -> {
                    presentationTreeBuilder.text("方法后1-1", null);
                    presentationTreeBuilder.list(ptb -> {
                        System.out.println("clickHandlerScope 方法后1-2");
                        ptb.text("方法后1-2", null);
                        return Unit.INSTANCE;
                    });
                    presentationTreeBuilder.text("方法后1-3", null);
                    return Unit.INSTANCE;
                });
                inlayTreeSink.addPresentation(endOfLinePosition, new ArrayList<>(), "方法后2-tooltip", true, presentationTreeBuilder -> {
                    presentationTreeBuilder.collapsibleList(CollapseState.Collapsed, ptb -> {
                                System.out.println("clickHandlerScope 方法后2--k");
                                ptb.text("方法后2--k", null);
                                return Unit.INSTANCE;
                            }
                            , ptb -> {
                                System.out.println("clickHandlerScope 方法后2--b");
                                ptb.text("方法后2--b", null);
                                return Unit.INSTANCE;
                            }
                    );
                   /* presentationTreeBuilder.clickHandlerScope(new InlayActionData(new PsiPointerInlayActionPayload(pointer), "方法后2"), ptb -> {
                        System.out.println("clickHandlerScope 方法后2");
                        ptb.text("方法后2", null);
                        return Unit.INSTANCE;
                    });*/
                    return Unit.INSTANCE;
                });

            }
        }
    }

    @Override
    public void collectFromElementForActions(@NotNull PsiElement element, @NotNull InlayTreeSink sink) {
        SharedBypassCollector.super.collectFromElementForActions(element, sink);
    }
}