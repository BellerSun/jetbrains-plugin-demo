package com.aixcoder.jbpadapter.model;

import com.aixcoder.jbpadapter.enums.UsedType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 使用到的标识符
 */
@Data
@NoArgsConstructor
public class UsedIdentifier {

    /**
     * 文件
     */
    private PsiFile psiFileFrom;
    /**
     * 标识符节点
     */
    private PsiElement psiElemFrom;


    /**
     * 引用的类型
     */
    private UsedType usedType;

    /**
     * 被引用的文件.可能为空。比如引用的是package，那就没有目标文件
     */
    private PsiFile psiFileTo;
    /**
     * 被引用的标识符在该文件定义的节点
     */
    private PsiElement psiElemTo;

    public UsedIdentifier(PsiElement psiElemFrom, UsedType usedType, PsiElement psiElemTo) {
        this.psiElemFrom = psiElemFrom;
        this.psiFileFrom = psiElemFrom.getContainingFile();
        this.psiElemTo = psiElemTo;
        this.psiFileTo = psiElemTo.getContainingFile();
        this.usedType = usedType;
    }
}
