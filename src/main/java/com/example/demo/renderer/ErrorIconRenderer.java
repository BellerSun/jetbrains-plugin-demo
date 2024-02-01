package com.example.demo.renderer;

import com.intellij.codeInsight.hints.presentation.InputHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ErrorIconRenderer implements EditorCustomElementRenderer, InputHandler {
    private final Editor editor;
    private final Project myProject;
    private final int startOffset;

    public ErrorIconRenderer(Editor editor, Project project, int starOffset) {
        this.editor = editor;
        this.myProject = project;
        this.startOffset = starOffset;
    }

    @Override
    public void mouseClicked(@NotNull MouseEvent mouseEvent, @NotNull Point point) {
        System.out.println("mouseClicked");
    }

    @Override
    public void mouseExited() {
        ((EditorImpl) this.editor).setCustomCursor(this, Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    @Override
    public void mouseMoved(@NotNull MouseEvent mouseEvent, @NotNull Point point) {
        ((EditorImpl) this.editor).setCustomCursor(this, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        return MyIcons.stocking.getIconWidth();
    }

    @Override
    public int calcHeightInPixels(@NotNull Inlay inlay) {
        return MyIcons.stocking.getIconHeight();
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
        Icon consoleIcon = MyIcons.stocking;
        int curX = r.x + r.width / 2 - consoleIcon.getIconWidth() / 2;
        int curY = r.y + r.height / 2 - consoleIcon.getIconHeight() / 2;
        if (curX >= 0 && curY >= 0) {
            consoleIcon.paintIcon(inlay.getEditor().getComponent(), g, curX, curY);
        }
    }

}
