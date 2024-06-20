package com.example.demo.psi.modifier.property;

import com.intellij.psi.PsiElement;
import com.example.demo.utils.AixPsiUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PsiModifierPropertyAnalyzerAbs implements PsiModifierPropertyAnalyzer {


    @Override
    public Set<String> getAllModifierProperty(ModifierPropertyHolder psiAnalyzerByLanguage, PsiElement psi) {
        final Collection<PsiElement> children = AixPsiUtils.getChildren(psi);
        final String modifierElemType = this.getModifierElemType(psiAnalyzerByLanguage);
        if (modifierElemType == null) {
            return Collections.emptySet();
        }
        // 跟进表type，拿到外层的标志们
        final List<PsiElement> modifiersOuter = children.stream()
                .filter(c -> {
                    final String debuggerName = AixPsiUtils.abstractPsiDebuggerName(c);
                    return null != debuggerName && debuggerName.startsWith(modifierElemType);
                })
                .collect(Collectors.toList());
        // 外层的type节点们，铺开，要是你自己就代表，那就返回你自己，如果你内部有，就返回你内部
        final List<PsiElement> modifiersFlat = modifiersOuter.stream()
                .flatMap(m -> psiAnalyzerByLanguage.flatModifierElem(m).stream())
                .collect(Collectors.toList());

        // 看看最终的标志们，有没有你要
        return modifiersFlat.stream()
                .map(p -> p.getNode().getText())
                .filter(t -> !t.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasModifierProperty(ModifierPropertyHolder psiAnalyzerByLanguage, PsiElement psi, String modifierProperty) {
        final Set<String> allModifierProperty = this.getAllModifierProperty(psiAnalyzerByLanguage, psi);
        // 看看最终的标志们，有没有你要的
        final boolean anyMatch = allModifierProperty.stream()
                .anyMatch(t -> t.trim().equals(modifierProperty));
        return anyMatch;
    }

    protected abstract String getModifierElemType(ModifierPropertyHolder psiAnalyzerByLanguage);
}
