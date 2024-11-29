package com.aixcoder.jbpadapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支持的种类： package, class, method, constructor, field, variable, unknown
 */
@Getter
@AllArgsConstructor
public enum UsedType {
    UNKNOWN("未知的"),
    PACKAGE("包引用"),
    CLASS("类引用"),
    METHOD("方法引用"),
    CONSTRUCTOR("构造方法引用"),
    FIELD("作为字段引用"),
    VARIABLE("作为变量引用"),

    ;

    private final String desc;
}
