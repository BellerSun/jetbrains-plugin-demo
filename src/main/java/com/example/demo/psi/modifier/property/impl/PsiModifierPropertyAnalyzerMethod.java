package com.example.demo.psi.modifier.property.impl;

import com.example.demo.psi.modifier.property.ModifierPropertyHolder;
import com.example.demo.psi.modifier.property.PsiModifierPropertyAnalyzerAbs;

public class PsiModifierPropertyAnalyzerMethod extends PsiModifierPropertyAnalyzerAbs {

    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiModifierPropertyAnalyzerMethod INSTANCE = new PsiModifierPropertyAnalyzerMethod();
    }

    public static PsiModifierPropertyAnalyzerMethod getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiModifierPropertyAnalyzerMethod() {
    }

    @Override
    protected String getModifierElemType(ModifierPropertyHolder psiAnalyzerByLanguage) {
        return psiAnalyzerByLanguage.getMethodModifierElemType();
    }
}
