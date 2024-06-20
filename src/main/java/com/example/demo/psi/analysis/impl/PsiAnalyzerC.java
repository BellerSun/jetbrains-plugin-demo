package com.example.demo.psi.analysis.impl;

import com.example.demo.psi.analysis.PsiAnalyzerAbs;
import com.example.demo.psi.analysis.PsiLanguageHasAbstract;
import com.example.demo.psi.analysis.PsiMethodSurroundByChar;
import com.example.demo.psi.modifier.property.AixPsiModifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PsiAnalyzerC extends PsiAnalyzerAbs implements PsiLanguageHasAbstract, PsiMethodSurroundByChar {
    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiAnalyzerC INSTANCE = new PsiAnalyzerC();
    }

    public static PsiAnalyzerC getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiAnalyzerC() {
    }

    private static final Set<String> PIS_METHOD_ELE_TYPE = new HashSet<>(Arrays.asList("FUNCTION_DEFINITION", "FUNCTION_PREDEFINITION"));
    private static final String PIS_METHOD_CODE_ELE_TYPE = "LAZY_BLOCK";
    private static final String PIS_MODIFIER_ELE_TYPE = "OCKeyword";

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
        return AixPsiModifier.VIRTUAL;
    }

    @Override
    public boolean modifierElemNeedFlat() {
        return false;
    }
}
