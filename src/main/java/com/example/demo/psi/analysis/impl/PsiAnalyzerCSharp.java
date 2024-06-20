package com.example.demo.psi.analysis.impl;

import com.example.demo.psi.analysis.PsiAnalyzerAbs;
import com.example.demo.psi.analysis.PsiMethodSurroundByChar;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PsiAnalyzerCSharp extends PsiAnalyzerAbs implements PsiMethodSurroundByChar {
    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiAnalyzerCSharp INSTANCE = new PsiAnalyzerCSharp();
    }

    public static PsiAnalyzerCSharp getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiAnalyzerCSharp() {
    }

    private static final Set<String> PIS_METHOD_ELE_TYPE = new HashSet<>(Collections.singleton("elementType cs:method-declaration"));
    private static final String PIS_METHOD_CODE_ELE_TYPE = "elementType cs:block-list";

    @Override
    public String getPsiMethodCodeElemType() {
        return PIS_METHOD_CODE_ELE_TYPE;
    }


    @Override
    protected Set<String> supportMethodEleType() {
        return PIS_METHOD_ELE_TYPE;
    }
}
