package com.example.demo.psi.analysis.impl;

import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.psi.PsiElement;
import com.example.demo.psi.analysis.PsiAnalyzerByLanguage;
import com.example.demo.psi.enumerations.AixPsiElemType;

public class PsiAnalyzerByLanguageBlackHole implements PsiAnalyzerByLanguage {

    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiAnalyzerByLanguageBlackHole INSTANCE = new PsiAnalyzerByLanguageBlackHole();
    }

    public static PsiAnalyzerByLanguageBlackHole getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiAnalyzerByLanguageBlackHole() {
    }

    @Override
    public AixPsiElemType getElemType(PsiElement psi) {
        return AixPsiElemType.UNKNOWN;
    }

    @Override
    public boolean elemLELayerSingleClassOrMethod(PsiElement psi) {
        return false;
    }

    @Override
    public boolean elemSuperHasMethod(PsiElement psi) {
        return false;
    }

    @Override
    public boolean elemSuperHasMoreClass(PsiElement psi) {
        return false;
    }

    @Override
    public PsiElement abstractClassElem(EditorImpl editor) {
        return null;
    }

    @Override
    public String abstractClassName(EditorImpl editor) {
        return "";
    }
}
