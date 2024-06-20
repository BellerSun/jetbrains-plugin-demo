package com.example.demo.psi.modifier.property;

import com.intellij.psi.PsiElement;
import com.example.demo.psi.enumerations.AixPsiElemType;
import com.example.demo.psi.modifier.property.impl.PsiModifierPropertyAnalyzerBlackHole;
import com.example.demo.psi.modifier.property.impl.PsiModifierPropertyAnalyzerClass;
import com.example.demo.psi.modifier.property.impl.PsiModifierPropertyAnalyzerMethod;

import java.util.Set;

public interface PsiModifierPropertyAnalyzer {

    /**
     * 查询该psi节点所有的修饰符
     *
     * @param psiAnalyzerByLanguage 语言分析的实现
     * @param psi                   psi节点
     * @return 所有修饰符
     */
    Set<String> getAllModifierProperty(ModifierPropertyHolder psiAnalyzerByLanguage, final PsiElement psi);

    /**
     * 查询该psi节点有没有指定的修饰符
     *
     * @param psiAnalyzerByLanguage 语言分析的实现
     * @param psi                   psi节点
     * @param modifierProperty      修饰符
     * @return 是否有
     */
    boolean hasModifierProperty(ModifierPropertyHolder psiAnalyzerByLanguage, final PsiElement psi, final String modifierProperty);


    @SuppressWarnings("all")
    static PsiModifierPropertyAnalyzer getInstance(AixPsiElemType aixPsiElemType) {
        switch (aixPsiElemType) {
            case METHOD:
                return PsiModifierPropertyAnalyzerMethod.getInstance();
            case CLASS:
                return PsiModifierPropertyAnalyzerClass.getInstance();
            default:
                return PsiModifierPropertyAnalyzerBlackHole.getInstance();
        }
    }
}
