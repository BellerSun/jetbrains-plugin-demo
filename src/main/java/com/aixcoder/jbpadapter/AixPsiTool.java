package com.aixcoder.jbpadapter;

import com.aixcoder.jbpadapter.model.UsedIdentifier;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

/**
 * 和psiFile有关系的，在PsiFileWrapper，没关系的在这里。当然，业务要是再细分，这里可以再次分发。
 *
 * @param <T_METHOD>
 */
public abstract class AixPsiTool<T_METHOD extends PsiElement> {

    /**
     * 获取该节点内所有的方法引用。
     */
    public List<T_METHOD> getUsedMethods(Project project, PsiElement elem) {
        return new ArrayList<>();
    }

    /**
     * 获取该文件内的测试用例列表。
     */
    public boolean isTestCase(T_METHOD method) {
        return false;
    }

    public String getMethodSignature(T_METHOD method) {
        throw new RuntimeException("not support");
    }

    /**
     * 获取引用的所有信息
     */
    public List<UsedIdentifier> getUsedDefinitions(Project project, PsiElement elem) {
        return new ArrayList<>();
    }

    /**
     * 获取<b>被</b>引用的所有信息
     */
    public List<UsedIdentifier> getRefDefinitions(Project project, PsiElement elem) {
        return new ArrayList<>();
    }
}
