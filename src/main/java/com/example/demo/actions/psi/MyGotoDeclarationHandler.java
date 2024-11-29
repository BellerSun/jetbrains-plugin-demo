package com.example.demo.actions.psi;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public class MyGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement != null) {
            // 获取跳转目标
            PsiReference reference = sourceElement.getReference();
            if (reference != null) {
                PsiElement target = reference.resolve();
                if (target != null) {
                    return new PsiElement[]{target};
                }
            }
        }
        return PsiElement.EMPTY_ARRAY;
    }
}
