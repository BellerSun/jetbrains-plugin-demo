package com.example.demo.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;
import com.intellij.vcs.commit.CommitWorkflowHandler;
import com.intellij.vcs.commit.CommitWorkflowUi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VcsUtil {

    public static List<Change> getChangesUsingUI(@NotNull AnActionEvent event) {
        CommitWorkflowHandler workflowHandler = event.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
        if (workflowHandler == null) {
            return null;
        }

        CommitWorkflowUi workflowUi = ((AbstractCommitWorkflowHandler<?, ?>) workflowHandler).getUi();

        return getChangesByUI(workflowUi);
    }

    public static List<Change> getChangesUsingReflection(@NotNull AnActionEvent event) {
        CommitWorkflowUi workflowUi = null;
        try {
            Class<?> vcsDataKeysClass = Class.forName("com.intellij.openapi.vcs.VcsDataKeys");
            Field commitWorkflowUiField = vcsDataKeysClass.getField("COMMIT_WORKFLOW_UI");
            DataKey<CommitWorkflowUi> commitWorkflowUiDataKey = (DataKey<CommitWorkflowUi>) commitWorkflowUiField.get(null);
            workflowUi = event.getData(commitWorkflowUiDataKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (workflowUi == null) {
            return null;
        }
        return getChangesByUI(workflowUi);
    }

    @Nullable
    private static List<Change> getChangesByUI(CommitWorkflowUi workflowUi) {
        List<Change> includedChanges = workflowUi.getIncludedChanges();
        List<Change> unversionedChanges = workflowUi.getIncludedUnversionedFiles()
                .stream()
                .map(filePath -> new Change(null, new CurrentContentRevision(filePath)))
                .collect(Collectors.toList());

        if (!includedChanges.isEmpty() || !unversionedChanges.isEmpty()) {
            List<Change> allChanges = new ArrayList<>();
            allChanges.addAll(includedChanges);
            allChanges.addAll(unversionedChanges);
            return allChanges;
        }
        return null;
    }
}
