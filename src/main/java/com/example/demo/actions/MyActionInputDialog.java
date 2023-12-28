package com.example.demo.actions;

import com.example.demo.frames.InputLenLimitFrame;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MyActionInputDialog extends AnAction {


    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.resizable(false);
        dialogBuilder.setTitle("TextLen");
        dialogBuilder.removeAllActions();

        dialogBuilder.centerPanel(new InputLenLimitFrame().getRoot());
        dialogBuilder.showModal(true);
    }
}
