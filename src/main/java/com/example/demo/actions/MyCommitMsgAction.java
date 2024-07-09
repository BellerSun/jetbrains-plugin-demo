package com.example.demo.actions;

import com.example.demo.service.VcsPromptingCalculator;
import com.example.demo.utils.VcsUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyCommitMsgAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();

        List<Change> changes = VcsUtil.getChangesUsingUI(event);
        if (changes == null) {
            return;
        }

        VcsPromptingCalculator vcsPromptingCalculator = ServiceManager.getService(project, VcsPromptingCalculator.class);
        String diffSummary = vcsPromptingCalculator.getDiffSummary(changes);
        if (diffSummary.isEmpty() || diffSummary.equals("\n")) {
            return;
        }


        System.out.println(diffSummary);
    }
}
