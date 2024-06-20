package com.example.demo.utils;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AixPsiUtils {

    public static List<PsiClass> getPsiClassBlocksByPsiFile(@NotNull PsiFile psiFile) {
        PsiElement[] psiElements = psiFile.getChildren();

        List<PsiClass> classes = new ArrayList<>();
        for (PsiElement child : psiElements) {
            if (child instanceof PsiClassImpl) {
                PsiClass c1 = (PsiClass) child;
                classes.add(c1);
            }
        }
        return classes;
    }

    @Nullable
    @SuppressWarnings("all")
    public static String abstractPsiDebuggerName(PsiElement psi) {
        try {
            return abstractPsiDebuggerNameDone(psi);
        } catch (Exception e) {
            return null;
        }
    }

    private static String abstractPsiDebuggerNameDone(PsiElement psi) {
        final Optional<IElementType> elementTypeOpt = Optional.ofNullable(psi).map(PsiElement::getNode).map(ASTNode::getElementType);
        if (!elementTypeOpt.isPresent()) {
            return null;
        }
        final IElementType elementType = elementTypeOpt.get();
        return Optional.of(elementType).map(AixPsiUtils::getDebugName).orElseGet(elementType::toString);
    }

    public static int getOffset(final PsiElement method) {
        try {
            return method.getTextOffset();
        } catch (Exception e) {
            return method.getNode().getStartOffset();
        }
    }

    private static final ThreadLocal<Field> myDebugNameField = ThreadLocal.withInitial(() -> {
        try {
            final Field field = IElementType.class.getDeclaredField("myDebugName");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    });

    @Nullable
    private static String getDebugName(IElementType elementType) {
        try {
            if (myDebugNameField.get() == null) {
                return null;
            }
            return (String) myDebugNameField.get().get(elementType);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static Collection<PsiElement> getChildren(PsiElement psi) {
        final Collection<PsiElement> result = new ArrayList<>();
        PsiElement childNode = psi.getFirstChild();
        while (childNode != null) {
            if (!(childNode instanceof PsiWhiteSpace)) {
                result.add(childNode);
            }
            childNode = childNode.getNextSibling();
        }
        return result;
    }
}
