package com.example.demo.psi.analysis.impl;

import com.example.demo.psi.analysis.PsiAnalyzerAbs;
import com.example.demo.psi.analysis.PsiLanguageHasAbstract;
import com.example.demo.psi.analysis.PsiMethodSurroundByChar;
import com.example.demo.psi.modifier.property.AixPsiModifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PsiAnalyzerJSXTSX extends PsiAnalyzerAbs implements PsiLanguageHasAbstract, PsiMethodSurroundByChar {
    // 线程安全懒加载的单例
    private static class SingletonHolder {
        private static final PsiAnalyzerJSXTSX INSTANCE = new PsiAnalyzerJSXTSX();
    }

    public static PsiAnalyzerJSXTSX getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiAnalyzerJSXTSX() {
    }

    private static final Set<String> PIS_METHOD_ELE_TYPE = new HashSet<>(Arrays.asList(
            "JS:FUNCTION_DECLARATION"
            , "JS:TYPESCRIPT_FUNCTION"
            , "JS:FUNCTION_EXPRESSION"
            , "FUNCTION_DECLARATION"
            , "TYPESCRIPT_FUNCTION_DECLARATION"
            , "TYPESCRIPT_FUNCTION"
            , "FUNCTION_EXPRESSION"
            , "TYPESCRIPT_FUNCTION_EXPRESSION"
            , "FLOW_JS_FUNCTION"
            , "FLOW_JS_FUNCTION_EXPRESSION"
            , "FLOW_JS_FUNCTION_DECLARATION"
    ));
    private static final String PIS_METHOD_CODE_ELE_TYPE = "BLOCK_STATEMENT";

    private static final String PIS_MODIFIER_ELE_TYPE = "ATTRIBUTE_LIST";

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
