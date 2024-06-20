package com.example.demo.psi.analysis.domain;

import lombok.Data;

/**
 * psi解析时候，方法节点内部的代码块儿节点的状态
 */
@Data
public class MethodCodeBlockState {

    /**
     * 检测到了代码块
     */
    private boolean hasCodeBlock = false;
    /**
     * 有开始标志
     */
    private boolean hasStartSymbol = false;
    /**
     * 没开始标志
     */
    private boolean hasEndSymbol = false;


    public boolean isWholeMethod() {
        return hasCodeBlock() && hasStartSymbol() && hasEndSymbol();
    }


    public boolean hasCodeBlock() {
        return hasCodeBlock;
    }

    public boolean hasStartSymbol() {
        return hasStartSymbol;
    }

    public boolean hasEndSymbol() {
        return hasEndSymbol;
    }
}
