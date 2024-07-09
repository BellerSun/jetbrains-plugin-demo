package com.example.demo.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.vcs.log.VcsFullCommitDetails;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service(value = {Service.Level.PROJECT})
public final class VcsPromptingCalculator {
    private final Project project;
    private final List<PathMatcher> defaultIgnorePatterns;

    public VcsPromptingCalculator(Project project) {
        this.project = project;
        this.defaultIgnorePatterns = Arrays.asList("**/*.json", "**/*.jsonl", "**/*.txt", "**/*.log", "**/*.tmp", "**/*.temp", "**/*.bak", "**/*.swp", "**/*.svg")
                .stream()
                .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern))
                .collect(Collectors.toList());
    }

    public String getDiffSummary(List<Change> changes) {
        DiffSimplifier diffSimplifier = ServiceManager.getService(this.project, DiffSimplifier.class);
        return diffSimplifier.simplifyDiff(changes, this.defaultIgnorePatterns);
    }

    public String generateCommitMessage(List<VcsFullCommitDetails> commitDetails, List<Change> changes, Project project, List<PathMatcher> ignorePatterns) {
        if (ignorePatterns == null) {
            ignorePatterns = this.defaultIgnorePatterns;
        }

        DiffSimplifier diffSimplifier = ServiceManager.getService(project, DiffSimplifier.class);
        String simplifiedDiff = diffSimplifier.simplifyDiff(changes, ignorePatterns);
        if (simplifiedDiff.isEmpty()) {
            return null;
        }

        String formattedDiff = DiffSimplifier.formatDiff(simplifiedDiff);
        StringWriter stringWriter = new StringWriter();

        if (!commitDetails.isEmpty()) {
            stringWriter.write("Commit Message: ");
            for (VcsFullCommitDetails detail : commitDetails) {
                stringWriter.write(detail.getFullMessage() + "\n\n");
            }
        }

        stringWriter.write("Changes:\n\n```patch\n" + formattedDiff + "\n```");
        return stringWriter.toString();
    }

    public List<Change> getAllChanges() {
        ChangeListManager changeListManager = ChangeListManager.getInstance(this.project);
        return changeListManager.getChangeLists()
                .stream()
                .flatMap(changeList -> changeList.getChanges().stream())
                .collect(Collectors.toList());
    }
}
