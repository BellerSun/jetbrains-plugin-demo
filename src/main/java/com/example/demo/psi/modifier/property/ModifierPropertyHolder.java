package com.example.demo.psi.modifier.property;

import com.example.demo.psi.PsiLanguageFeature;
import com.example.demo.psi.enumerations.AixPsiElemType;
import com.intellij.psi.PsiElement;
import com.example.demo.utils.AixPsiUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * 持有修饰符属性的语言
 */
public interface ModifierPropertyHolder extends PsiLanguageFeature {

    default boolean hasModifierProperty(AixPsiElemType elemType, PsiElement psiElem, String modifierProperty) {
        final PsiModifierPropertyAnalyzer modifierPropertyAnalyzer = PsiModifierPropertyAnalyzer.getInstance(elemType);
        return modifierPropertyAnalyzer.hasModifierProperty(this, psiElem, modifierProperty);
    }

    default String getMethodModifierElemType() {
        return "";
    }

    default String getClassModifierElemType() {
        return "";
    }

    default Collection<PsiElement> flatModifierElem(PsiElement modifier) {
        return this.modifierElemNeedFlat() ? AixPsiUtils.getChildren(modifier) : Collections.singletonList(modifier);
    }


    /**
     * 标志节点是不是需要铺开看子节点
     */
    default boolean modifierElemNeedFlat() {
        return true;
    }
}
