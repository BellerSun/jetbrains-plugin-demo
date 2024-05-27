package com.example.demo.actions;

import com.example.demo.utils.JbrToolKit;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class MyActionDetectSelectLang extends AnAction {


    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        PsiFile data = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
        if (editor == null) {
            return;
        }
        String selectedText = editor.getSelectionModel().getSelectedText();
        Language language = JbrToolKit.Language.detectLanguage(project, selectedText);
        System.out.println("-----------------------------");
        System.out.println("selectedText:" + selectedText);
        System.out.println("-----------");
        System.out.println("language:" + language);
        System.out.println("-----------------------------");
    }
}
