package com.example.demo.psi.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AixPsiElemType {
    UNKNOWN("不道啊我", "UNKNOWN"),
    METHOD("方法", "METHOD"),
    CLASS("类", "CLASS"),


    ;
    /**
     * 给开发人员看的字段,可不兴用啊兄弟
     */
    private final String description;
    private final String code;

}
