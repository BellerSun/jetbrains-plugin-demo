package com.example.demo.psi.analysis;

import com.example.demo.psi.PsiLanguageFeature;
import com.example.demo.psi.analysis.impl.*;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.psi.PsiElement;
import com.example.demo.psi.enumerations.AixPsiElemType;
/**
 * psi语法分析--每种语言都要有一个实现，还有一个默认实现
 */
public interface PsiAnalyzerByLanguage extends PsiLanguageFeature {

    /**
     * 计算psi节点的类型
     *
     * @param psi psi节点
     * @return psi节点的类型
     */
    AixPsiElemType getElemType(PsiElement psi);

    /**
     * 判断psi节点，包括自己在内，向上是不是只 <= 1 个（class或method）类型节点。
     *
     * @param psi psi节点
     * @return 是否只在一个或者零个class节点内
     */
    boolean elemLELayerSingleClassOrMethod(final PsiElement psi);

    /**
     * psi节点向上找，是不是有方法节点
     *
     * @param psi psi节点
     * @return 是否有方法节点
     */
    boolean elemSuperHasMethod(final PsiElement psi);

    /**
     * psi节点向上找，是不是有一个以上class
     *
     * @param psi psi节点
     * @return 是否有一个以上class
     */
    boolean elemSuperHasMoreClass(final PsiElement psi);

    /**
     * 尝试提取出一个class节点，如果没有，就返回null
     * @param editor 编辑器
     * @return 语法分析实现
     */
    PsiElement abstractClassElem(EditorImpl editor);

    String abstractClassName(EditorImpl editor);


    /**
     * 帮外层挑选一个实现
     *
     * @param editor 编辑器
     * @return 语法分析实现
     */
    @SuppressWarnings("all")
    static PsiAnalyzerByLanguage chooseImplByEditor(EditorImpl editor) {
        return PsiAnalyzerJava.getInstance();
    }


}
