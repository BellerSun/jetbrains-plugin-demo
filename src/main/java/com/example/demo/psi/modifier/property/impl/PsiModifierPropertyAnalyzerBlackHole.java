package com.example.demo.psi.modifier.property.impl;

import com.intellij.psi.PsiElement;
import com.example.demo.psi.modifier.property.ModifierPropertyHolder;
import com.example.demo.psi.modifier.property.PsiModifierPropertyAnalyzer;

import java.util.Collections;
import java.util.Set;

public class PsiModifierPropertyAnalyzerBlackHole implements PsiModifierPropertyAnalyzer {

    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiModifierPropertyAnalyzerBlackHole INSTANCE = new PsiModifierPropertyAnalyzerBlackHole();
    }

    public static PsiModifierPropertyAnalyzerBlackHole getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiModifierPropertyAnalyzerBlackHole() {
    }


    @Override
    public boolean hasModifierProperty(ModifierPropertyHolder psiAnalyzerByLanguage, PsiElement psi, String modifierProperty) {
        return false;
    }

    @Override
    public Set<String> getAllModifierProperty(ModifierPropertyHolder psiAnalyzerByLanguage, PsiElement psi) {
        return Collections.emptySet();
    }

}
