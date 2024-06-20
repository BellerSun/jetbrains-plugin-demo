package com.example.demo.psi.analysis.impl;

import com.example.demo.psi.analysis.PsiAnalyzerAbs;
import com.example.demo.psi.analysis.PsiMethodSurroundByChar;

public class PsiAnalyzerGO extends PsiAnalyzerAbs implements PsiMethodSurroundByChar {
    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiAnalyzerGO INSTANCE = new PsiAnalyzerGO();
    }

    public static PsiAnalyzerGO getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiAnalyzerGO() {
    }

    private static final String PIS_METHOD_CODE_ELE_TYPE = "BLOCK";

    @Override
    public String getPsiMethodCodeElemType() {
        return PIS_METHOD_CODE_ELE_TYPE;
    }

}
