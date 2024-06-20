package com.example.demo.psi.analysis.impl;

import com.example.demo.psi.analysis.PsiAnalyzerAbs;

public class PsiAnalyzerDefault extends PsiAnalyzerAbs {

    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiAnalyzerDefault INSTANCE = new PsiAnalyzerDefault();
    }

    public static PsiAnalyzerDefault getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiAnalyzerDefault() {
    }

}
