package com.example.demo.psi.modifier.property;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
public enum AixPsiModifier {
    PUBLIC("public"),
    PROTECTED("protected"),
    PRIVATE("private"),
    PACKAGE_LOCAL("packageLocal"),
    STATIC("static"),
    ABSTRACT("abstract"),
    VIRTUAL("virtual"),
    FINAL("final"),
    NATIVE("native"),
    SYNCHRONIZED("synchronized"),
    STRICTFP("strictfp"),
    TRANSIENT("transient"),
    VOLATILE("volatile"),
    DEFAULT("default"),
    OPEN("open"),
    TRANSITIVE("transitive"),
    SEALED("sealed"),
    NON_SEALED("non-sealed"),


    ;

    private final String txt;

    public static AixPsiModifier[] getModifiers() {
        return values();
    }


    /**
     * 可能为null
     */
    public static AixPsiModifier of(String txt) {
        for (AixPsiModifier modifier : values()) {
            if (modifier.getTxt().equalsIgnoreCase(txt)) {
                return modifier;
            }
        }
        return null;
    }
}
