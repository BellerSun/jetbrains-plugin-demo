package com.example.demo.psi.analysis;

import com.example.demo.psi.PsiLanguageFeature;
import com.example.demo.psi.enumerations.AixPsiElemType;
import com.example.demo.psi.modifier.property.AixPsiModifier;
import com.example.demo.psi.modifier.property.ModifierPropertyHolder;
import com.intellij.psi.PsiElement;

/**
 * 有abstract这个概念的语言.哪个语言要是有抽象这个概念，就impl我吧
 */
public interface PsiLanguageHasAbstract extends PsiLanguageFeature, ModifierPropertyHolder {
    /**
     * 判断方法是不是抽象方法
     *
     * @param method method的psi节点。调用前请保证{@link  PsiAnalyzerByLanguage#getElemType(PsiElement)}结果为{@link AixPsiElemType#METHOD}。这里不会帮你二次检查
     * @return 是否是方法
     */
    default boolean methodIsAbstract(final PsiElement method) {
        final AixPsiModifier psiModifierOfAbstract = this.supportModifierPropertyAbstract();
        if (null == psiModifierOfAbstract) {
            return false;
        }
        return this.hasModifierProperty(AixPsiElemType.METHOD, method, psiModifierOfAbstract.getTxt());
    }

    /**
     * @return 什么样子的子节点属性代表这个方法是抽象的，null代表改语言不支持抽象这个概念
     */
    default AixPsiModifier supportModifierPropertyAbstract() {
        return null;
    }


    static PsiLanguageHasAbstract getInstance(PsiAnalyzerByLanguage psiAnalyzerByLanguage) {
        return psiAnalyzerByLanguage instanceof PsiLanguageHasAbstract ? (PsiLanguageHasAbstract) psiAnalyzerByLanguage : NOT_SUPPORT_LANGUAGE_IMPL;
    }

    /**
     * 不支持的语言类型默认实现。
     */
    PsiLanguageHasAbstract NOT_SUPPORT_LANGUAGE_IMPL = new PsiLanguageHasAbstract() {
        @Override
        public boolean methodIsAbstract(PsiElement method) {
            return false;
        }
    };

}
