package com.example.demo.psi.analysis;

import com.intellij.psi.PsiElement;
import com.example.demo.psi.PsiLanguageFeature;
import com.example.demo.psi.analysis.domain.MethodCodeBlockState;

/**
 * 方法是被某些括号包裹的语言，需要实现这个接口
 */
public interface PsiMethodSurroundByChar extends PsiLanguageFeature {

    /**
     * 获取方法内部的的代码块状态
     *
     * @param method 方法节点
     * @return 方法内部的代码块状态
     */
    default MethodCodeBlockState getMethodCodeBlockState(PsiElement method) {
        final MethodCodeBlockState methodCodeBlockState = new MethodCodeBlockState();
        final PsiElement codeBlock = this.getCodeBlock(method);
        if (null == codeBlock) {
            return methodCodeBlockState;
        }
        methodCodeBlockState.setHasCodeBlock(true);
        final String codeText = codeBlock.getText();

        // 正数第一个非空字符，是左括号。跳过空白字符
        final Character firstChar = abstractStrFirstNoneWhiteChar(codeText, true);
        final char openChar = this.getPsiMethodCodeOpenChar();
        methodCodeBlockState.setHasStartSymbol(firstChar != null && firstChar == openChar);
        // 倒数第一个非空字符，是右括号
        final Character lastChar = abstractStrFirstNoneWhiteChar(codeText, false);
        final char closeChar = this.getPsiMethodCodeCloseChar();
        methodCodeBlockState.setHasEndSymbol(lastChar != null && lastChar == closeChar);
        return methodCodeBlockState;
    }


    /**
     * 获取方法的代码块，如果没有，就返回null
     *
     * @param method 方法节点
     * @return 方法的代码块
     */
    default PsiElement getCodeBlock(PsiElement method) {
        final String codeBlockType = this.getPsiMethodCodeElemType();
        if (null == codeBlockType) {
            return null;
        }
        PsiElement codeBlock = null;
        final PsiElement[] childrenArr = method.getChildren();
        for (PsiElement child : childrenArr) {
            final String childEleType = child.getNode().getElementType().toString();
            if (codeBlockType.equals(childEleType)) {
                codeBlock = child;
                break;
            }
        }
        return codeBlock;
    }

    /**
     * @return 获取该语言方法内部的代表代码块儿的psi类型
     */
    String getPsiMethodCodeElemType();

    /**
     * @return 获取该语言方法代码块儿的正常开始标志.默认是左花括号
     */
    default char getPsiMethodCodeOpenChar() {
        return '{';
    }

    /**
     * @return 获取该语言方法代码块儿的正常结束标志.默认是右花括号
     */
    default char getPsiMethodCodeCloseChar() {
        return '}';
    }


    /**
     * 提取字符串第一个非空白字符
     *
     * @param codeText  代码文本
     * @param fromStart 从头开始提取还是从尾开始提取，true代表从头开始提取
     * @return 第一个非空白字符
     */
    private static Character abstractStrFirstNoneWhiteChar(String codeText, boolean fromStart) {
        if (null == codeText) {
            return null;
        }
        int index = fromStart ? 0 : codeText.length() - 1;
        while (fromStart ? index < codeText.length() && Character.isWhitespace(codeText.charAt(index))
                : index >= 0 && Character.isWhitespace(codeText.charAt(index))) {
            index = fromStart ? index + 1 : index - 1;
        }
        if (fromStart ? index == codeText.length() : index < 0) {
            return null;
        }
        return codeText.charAt(index);
    }


    static PsiMethodSurroundByChar getInstance(PsiAnalyzerByLanguage psiAnalyzerByLanguage) {
        return psiAnalyzerByLanguage instanceof PsiMethodSurroundByChar ? (PsiMethodSurroundByChar) psiAnalyzerByLanguage : NOT_SUPPORT_LANGUAGE_IMPL;
    }


    /**
     * 不支持的语言类型。
     */
    PsiMethodSurroundByChar NOT_SUPPORT_LANGUAGE_IMPL = new PsiMethodSurroundByChar() {

        @Override
        public MethodCodeBlockState getMethodCodeBlockState(PsiElement method) {
            final MethodCodeBlockState codeBlockState = new MethodCodeBlockState();
            // 不支持的语言，永远都有代码块和前后状态。宽松模式
            codeBlockState.setHasCodeBlock(true);
            codeBlockState.setHasStartSymbol(true);
            codeBlockState.setHasEndSymbol(true);
            return codeBlockState;
        }

        @Override
        public String getPsiMethodCodeElemType() {
            return null;
        }
    };

}
