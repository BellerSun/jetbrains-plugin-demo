package com.example.demo.inlay.hints.old;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface MyInlayHintsClickEvent {


    void click(@NotNull MouseEvent mouseEvent, @NotNull Point point, Editor editor, PsiElement method);
}
