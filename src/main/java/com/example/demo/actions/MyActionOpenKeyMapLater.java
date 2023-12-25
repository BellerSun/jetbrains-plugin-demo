package com.example.demo.actions;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.cl.PluginAwareClassLoader;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.keymap.impl.ui.ActionsTreeUtil;
import com.intellij.openapi.keymap.impl.ui.EditKeymapsDialog;
import com.intellij.openapi.keymap.impl.ui.KeymapPanel;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyActionOpenKeyMapLater extends AnAction {
    private static final PluginId CURRENT_PLUGIN_ID = MyActionOpenKeyMapLater.class.getClassLoader() instanceof PluginAwareClassLoader ? ((PluginAwareClassLoader) MyActionOpenKeyMapLater.class.getClassLoader()).getPluginId() : null;

    private static final ScheduledExecutorService SCHEDULED_POOL = Executors.newScheduledThreadPool(1);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.openShortcuts(e.getProject());
    }

    private void openShortcuts(Project project) {
        final String firstActionId = chooseFirstActionId();
        new EditKeymapsDialog(project, firstActionId).show();
    }

    private static String chooseFirstActionId() {
        if (null == CURRENT_PLUGIN_ID) {
            return null;
        }
        final String[] pluginActions = ActionManagerEx.getInstanceEx().getPluginActions(CURRENT_PLUGIN_ID);
        if (pluginActions.length == 0) {
            return null;
        }
        Arrays.sort(pluginActions, Comparator.comparing(ActionsTreeUtil::getTextToCompare));
        return pluginActions[0];
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
