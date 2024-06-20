package com.example.demo.psi.modifier.property.impl;

import com.example.demo.psi.modifier.property.ModifierPropertyHolder;
import com.example.demo.psi.modifier.property.PsiModifierPropertyAnalyzerAbs;

public class PsiModifierPropertyAnalyzerClass extends PsiModifierPropertyAnalyzerAbs {

    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiModifierPropertyAnalyzerClass INSTANCE = new PsiModifierPropertyAnalyzerClass();
    }

    public static PsiModifierPropertyAnalyzerClass getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiModifierPropertyAnalyzerClass() {
    }

    @Override
    protected String getModifierElemType(ModifierPropertyHolder psiAnalyzerByLanguage) {
        return psiAnalyzerByLanguage.getClassModifierElemType();
    }
}
