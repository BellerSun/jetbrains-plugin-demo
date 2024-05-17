package com.example.demo.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;

public class MyActionOpenFileChooser extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        final int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File fileToSave = fileChooser.getSelectedFile();
        System.out.println(fileToSave.getName());
    }

}
