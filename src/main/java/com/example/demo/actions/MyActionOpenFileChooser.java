package com.example.demo.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.fileChooser.impl.LocalFileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.messages.MessagesService;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyActionOpenFileChooser extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        FileChooserDescriptor singleFileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        //VirtualFile virtualFile = FileChooser.chooseFile(singleFileDescriptor, project, null);
        //System.out.println(virtualFile.getPath());
        var title = "file.chooser.new.file.title";
        var prompt = "file.chooser.new.file.prompt";
        var initial = "newFile.java";
        FileChooserPanel panel = e.getData(FileChooserPanel.DATA_KEY);
        var selection = new TextRange(0, 7);
/*        var directory = panel.currentDirectory();
        var validator = new InputValidatorEx() {

            @Override
            public @NlsContexts.DetailedDescription @Nullable String getErrorText(@NonNls String input) {
                input = input.trim();
                if (input.isEmpty()) {
                    return "file.name.validator.empty";
                }
                try {
                    if (directory.getFileSystem().getPath(input).isAbsolute()) {
                        return "file.name.validator.absolute";
                    }
                } catch (InvalidPathException e) {
                    return "file.name.validator.invalid";
                }
                return null;

            }
        };*/

        //String inputDialog = MessagesService.getInstance().showInputDialog(null, panel.getComponent(), prompt, title, null, initial, validator, selection, null);
        //System.out.println(inputDialog);

        String projectBasePath = project.getBasePath();

        Path path = Paths.get(projectBasePath);
        VirtualFileWrapper save = new LocalFileChooserFactory().createSaveFileDialog(new FileSaverDescriptor("title", "desc", "java"), project)
                .save(path,"aaa.java");
        System.out.println(save);


    }

}
