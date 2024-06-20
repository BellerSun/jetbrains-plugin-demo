package com.example.demo.utils.coverage.selected;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectInfoJava extends SelectInfo {
    private final List<PsiMethod> selectedMethods;

    public SelectInfoJava(Editor editor) {
        super(editor);
        this.selectedMethods = analysisSelectedMethods(editor);
    }

    private static List<PsiMethod> analysisSelectedMethods(Editor editor) {
        final ArrayList<PsiMethod> psiMethods = new ArrayList<>();
        if (editor.getProject() == null) {
            return psiMethods;
        }
        // 获取PsiFile对象
        final PsiFile psiFile = PsiManager.getInstance(editor.getProject()).findFile(editor.getVirtualFile());
        if (psiFile == null) {
            // 无法获取PsiFile对象
            return psiMethods;
        }

        // 确保这是一个Java文件
        if (!(psiFile instanceof PsiJavaFile)) {
            // 这不是一个Java文件
            return psiMethods;
        }

        final PsiJavaFile javaFile = (PsiJavaFile) psiFile;
        // 遍历文件中的所有类
        for (PsiClass psiClass : javaFile.getClasses()) {
            // 遍历类中的所有方法
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                System.out.println("Method: " + psiMethod.getName());
                System.out.println("Signature: " + psiMethod.getSignature(PsiSubstitutor.EMPTY));
            }
        }

        return psiMethods;
    }


    public List<String> getSelectedMethodSignatures() {
        if (selectedMethods.isEmpty()) {
            return new ArrayList<>();
        }
        return selectedMethods.stream().map(SelectInfoJava::getMethodSignature).collect(Collectors.toList());
    }


    private static String getMethodSignature(PsiMethod method) {
        StringBuilder signature = new StringBuilder();

        // 方法名称
        signature.append(method.getName());

        // 参数类型
        signature.append("(");
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            signature.append(getTypeSignature(parameter.getType()));
        }
        signature.append(")");

        // 返回类型
        signature.append(getTypeSignature(method.getReturnType()));

        return signature.toString();
    }

    private static String getTypeSignature(PsiType type) {
        if (type == null) {
            return "";
        }

        if (type instanceof PsiPrimitiveType) {
            // 基本类型
            return getPrimitiveTypeSignature((PsiPrimitiveType) type);
        } else if (type instanceof PsiArrayType) {
            // 数组类型
            return "[" + getTypeSignature(((PsiArrayType) type).getComponentType());
        } else {
            // 引用类型
            return "L" + type.getCanonicalText().replace('.', '/') + ";";
        }
    }

    private static String getPrimitiveTypeSignature(PsiPrimitiveType type) {
        switch (type.getCanonicalText()) {
            case "byte":
                return "B";
            case "char":
                return "C";
            case "double":
                return "D";
            case "float":
                return "F";
            case "int":
                return "I";
            case "long":
                return "J";
            case "short":
                return "S";
            case "boolean":
                return "Z";
            case "void":
                return "V";
            default:
                throw new IllegalArgumentException("未知的基本类型: " + type.getCanonicalText());
        }
    }
}
