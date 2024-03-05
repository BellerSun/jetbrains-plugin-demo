package com.example.demo.marks;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("all")
public class MyLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method != null) {

            // 创建一个标记，显示实现的数量
            return new LineMarkerInfo<>(
                    element,
                    element.getTextRange(),
                    null,  // 使用默认图标
                    99999,  // 显示实现的数量
                    null,
                    null,
                    GutterIconRenderer.Alignment.RIGHT
            );
        }

        return null;
    }

}