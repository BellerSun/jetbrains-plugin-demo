package com.example.demo.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.keymap.impl.ui.KeymapPanel;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyActionOpenKeyMapLater extends AnAction {
    private static final ScheduledExecutorService SCHEDULED_POOL = Executors.newScheduledThreadPool(1);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.openShortcuts(e.getProject());
    }

    private void openShortcuts(Project project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, KeymapPanel.class, this::navigationLater);
    }

    private void navigationLater(KeymapPanel keymapPanel) {
        SCHEDULED_POOL.schedule(() -> {
            System.out.println("selectAction com.test.MyActionOpenKeyMapLater");
            // excepted：expand the action, and navigation to the action
            // In fact: expand the action, not navigation to the action
            keymapPanel.selectAction("com.test.MyActionOpenKeyMapLater");
        }, 3, TimeUnit.SECONDS);
    }

}
