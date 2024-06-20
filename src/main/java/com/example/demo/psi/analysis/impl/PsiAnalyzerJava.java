package com.example.demo.psi.analysis.impl;


import com.example.demo.psi.analysis.PsiAnalyzerAbs;
import com.example.demo.psi.analysis.PsiLanguageHasAbstract;
import com.example.demo.psi.analysis.PsiMethodSurroundByChar;
import com.example.demo.psi.modifier.property.AixPsiModifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PsiAnalyzerJava extends PsiAnalyzerAbs implements PsiLanguageHasAbstract, PsiMethodSurroundByChar {
    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiAnalyzerJava INSTANCE = new PsiAnalyzerJava();
    }

    public static PsiAnalyzerJava getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiAnalyzerJava() {
    }

    private static final Set<String> PIS_METHOD_ELE_TYPE = new HashSet<>(Collections.singleton("METHOD"));
    private static final String PIS_METHOD_CODE_ELE_TYPE = "CODE_BLOCK";
    private static final String PIS_MODIFIER_ELE_TYPE = "MODIFIER_LIST";

    @Override
    public String getPsiMethodCodeElemType() {
        return PIS_METHOD_CODE_ELE_TYPE;
    }

    @Override
    protected Set<String> supportMethodEleType() {
        return PIS_METHOD_ELE_TYPE;
    }


    @Override
    public String getMethodModifierElemType() {
        return PIS_MODIFIER_ELE_TYPE;
    }

    @Override
    public String getClassModifierElemType() {
        return PIS_MODIFIER_ELE_TYPE;
    }

    @Override
    public AixPsiModifier supportModifierPropertyAbstract() {
        return AixPsiModifier.ABSTRACT;
    }
}
