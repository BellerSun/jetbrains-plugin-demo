package com.example.demo.psi.analysis;

import com.example.demo.psi.enumerations.AixPsiElemType;
import com.example.demo.psi.modifier.property.ModifierPropertyHolder;
import com.example.demo.utils.AixPsiUtils;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class PsiAnalyzerAbs implements PsiAnalyzerByLanguage, ModifierPropertyHolder {
    private static final Set<String> PIS_ELE_TYPE_CLASS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            // java
            "CLASS"
            // c\c++
            , "TYPE_ELEMENT"
            // python
            , "Py:CLASS_DECLARATION"
            , "CLASS_DECLARATION"
            // js ts jsx tsx
            , "JS:CLASS"
            , "JS:TYPESCRIPT_CLASS"
            , "TYPESCRIPT_CLASS"
    )));
    private static final Set<String> PIS_ELE_TYPE_CLASS_NAME = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            // java
            "CLASS"
            // c\c++
            , "TYPE_ELEMENT"
            // python
            , "Py:CLASS_DECLARATION"
            , "CLASS_DECLARATION"
            // js ts jsx tsx
            , "JS:CLASS"
            , "JS:TYPESCRIPT_CLASS"
            , "TYPESCRIPT_CLASS"
    )));
    private static final Set<String> PIS_ELE_TYPE_METHOD = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "METHOD"
            , "FUN"
            , "JS:TYPESCRIPT_FUNCTION"
            , "JS:FUNCTION_DECLARATION"
            , "METHOD_DECLARATION"
            , "FUNCTION_DECLARATION"
            , "FUNCTION_DEFINITION"
            , "FUNCTION_PREDEFINITION"
    )));


    @Override
    public AixPsiElemType getElemType(PsiElement psi) {
        final String elementTypeStr = AixPsiUtils.abstractPsiDebuggerName(psi);
        if (this.supportMethodEleType().contains(elementTypeStr)) {
            return AixPsiElemType.METHOD;
        }
        if (this.supportClassEleType().contains(elementTypeStr)) {
            return AixPsiElemType.CLASS;
        }
        return AixPsiElemType.UNKNOWN;
    }


    @Override
    public boolean elemLELayerSingleClassOrMethod(PsiElement psi) {
        int i = 0;
        // 向上遍历psi
        for (; psi != null; psi = psi.getParent()) {
            final String elementTypeStr = AixPsiUtils.abstractPsiDebuggerName(psi);
            final boolean elemIsClassOrMethod = this.supportClassEleType().contains(elementTypeStr) || this.supportMethodEleType().contains(elementTypeStr);
            if (elemIsClassOrMethod && (++i) > 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean elemSuperHasMethod(final PsiElement psi) {
        PsiElement parent = psi.getParent();
        while (parent != null) {
            final String elementTypeStr = AixPsiUtils.abstractPsiDebuggerName(parent);
            if (this.supportMethodEleType().contains(elementTypeStr)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    @Override
    public boolean elemSuperHasMoreClass(final PsiElement psi) {
        PsiElement parent = psi.getParent();
        int classCount = 0;
        while (parent != null) {
            final String elementTypeStr = AixPsiUtils.abstractPsiDebuggerName(parent);
            if (this.supportClassEleType().contains(elementTypeStr)) {
                classCount++;
                if (classCount > 1) {
                    return true;
                }
            }
            parent = parent.getParent();
        }
        return false;
    }

    @Override
    public String abstractClassName(EditorImpl editor) {
        PsiElement psiElement = this.abstractClassElem(editor);
        if (null==psiElement){
            return null;
        }
        final Project project = editor.getProject();
        if (null == project) {
            return null;
        }

        final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return null;
        }

        return null;
    }

    @Override
    public PsiElement abstractClassElem(EditorImpl editor) {
        final Project project = editor.getProject();
        if (null == project) {
            return null;
        }

        final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return null;
        }

        // 先查找第一层的class
        final PsiElement[] children = psiFile.getChildren();
        for (PsiElement child : children) {
            if (getElemType(child).equals(AixPsiElemType.CLASS)) {
                return child;
            }
        }

        // 再查找第二层的class
        for (PsiElement child : children) {
            final PsiElement[] children2 = child.getChildren();
            for (PsiElement child2 : children2) {
                if (getElemType(child2).equals(AixPsiElemType.CLASS)) {
                    return child2;
                }
            }
        }
        // 最多查找两层，如果还没有找到，则返回null
        return null;
    }

    /**
     * 返回支持的方法元素类型
     */
    protected Set<String> supportMethodEleType() {
        return PIS_ELE_TYPE_METHOD;
    }

    protected Set<String> supportClassEleType() {
        return PIS_ELE_TYPE_CLASS;
    }


}
