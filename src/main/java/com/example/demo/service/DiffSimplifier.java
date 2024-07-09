package com.example.demo.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vcs.changes.BinaryContentRevision;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.project.ProjectKt;
import org.jetbrains.annotations.NotNull;

import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class DiffSimplifier {
    private final Project project;
    private final Logger logger;
    private static final Pattern REVISION_REGEX = Pattern.compile("\\(revision [^)]+\\)");
    private static final String NO_NEWLINE_AT_END_OF_FILE = "\\ No newline at end of file";

    public DiffSimplifier(Project project) {
        this.project = project;
        this.logger = Logger.getInstance(DiffSimplifier.class);
    }

    public String simplifyDiff(List<Change> changes, List<PathMatcher> ignorePatterns) {
        String diffOutput = "";
        try {
            StringWriter diffWriter = new StringWriter();
            String projectBasePath = this.project.getBasePath();
            if (projectBasePath == null) {
                throw new RuntimeException("Project base path is null.");
            }

            List<Change> filteredChanges = changes.stream()
                    .filter(change -> !isChangeIgnored(change))
                    .filter(change -> isChangeMatched(change, ignorePatterns))
                    .collect(Collectors.toList());

            if (filteredChanges.isEmpty()) {
                return "";
            }

            List<FilePatch> patchList = IdeaTextPatchBuilder.buildPatch(this.project,
                    filteredChanges.subList(0, Math.min(filteredChanges.size(), 500)),
                    Path.of(projectBasePath),
                    false, true);

            UnifiedDiffWriter.write(this.project,
                    ProjectKt.getStateStore(this.project).getProjectBasePath(),
                    patchList,
                    diffWriter,
                    "\n",
                    null,
                    new ArrayList<>());

            diffOutput = diffWriter.toString();
            return diffOutput;
        } catch (Exception e) {
            throw new RuntimeException("Error calculating diff: " + e.getMessage(), e);
        }
    }

    @NotNull
    public static String formatDiff(@NotNull String diffString) {

        List<String> diffLines = List.of(diffString.split("\n"));
        List<String> processedLines = new ArrayList<>();

        for (int i = 0; i < diffLines.size(); i++) {
            String line = diffLines.get(i);

            if (shouldSkipLine(line)) {
                continue;
            }

            if (isNewFileMode(line, diffLines, i)) {
                processedLines.add("new file " + extractFilePath(diffLines.get(i + 2)));
                i += 2;
            } else if (isRenameMode(line, diffLines, i)) {
                processedLines.add("rename file from " + extractFilePath(diffLines.get(i)) + " to " + extractFilePath(diffLines.get(i + 1)));
                i += 3;
            } else if (isDeletedFileMode(line, diffLines, i)) {
                processedLines.add("delete file " + extractFilePath(diffLines.get(i + 1)));
                i += 2;
            } else if (isModifyFile(line, diffLines, i)) {
                processedLines.add("modify file " + extractFilePath(diffLines.get(i + 1)));
                i++;
            } else if (!line.trim().isEmpty()) {
                processedLines.add(line);
            }
        }

        return String.join("\n", processedLines);
    }

    private boolean isChangeIgnored(@NotNull Change change) {
        return isContentIgnored(change.getBeforeRevision()) || isContentIgnored(change.getAfterRevision());
    }

    private boolean isContentIgnored(ContentRevision contentRevision) {
        if (contentRevision instanceof CurrentContentRevision) {
            VirtualFile virtualFile = ((CurrentContentRevision) contentRevision).getVirtualFile();
            return virtualFile != null && (isBinaryOrTooLarge(contentRevision) || FileUtilRt.isTooLarge(virtualFile.getLength()));
        }
        return false;
    }

    private boolean isBinaryOrTooLarge(ContentRevision contentRevision) {
        return contentRevision instanceof BinaryContentRevision || contentRevision.getFile().getFileType().isBinary();
    }

    private boolean isChangeMatched(Change change, List<PathMatcher> ignorePatterns) {
        ContentRevision contentRevision = change.getAfterRevision();
        if (contentRevision != null) {
            Path filePath = Path.of(contentRevision.getFile().getPath());
            return ignorePatterns.stream().noneMatch(matcher -> matcher.matches(filePath));
        }
        return true;
    }

    private static boolean shouldSkipLine(String line) {
        return line.startsWith("diff --git ") ||
                line.startsWith("index:") ||
                line.startsWith("Index:") ||
                line.equals("===================================================================") ||
                line.contains(NO_NEWLINE_AT_END_OF_FILE) ||
                line.startsWith("---\t/dev/null") ||
                (line.startsWith("@@") && line.endsWith("@@"));
    }

    private static boolean isNewFileMode(String line, List<String> diffLines, int currentIndex) {
        return line.startsWith("new file mode") && diffLines.get(currentIndex + 1).startsWith("--- /dev/null");
    }

    private static boolean isRenameMode(String line, List<String> diffLines, int currentIndex) {
        return line.startsWith("rename from") && diffLines.get(currentIndex + 1).startsWith("rename to");
    }

    private static boolean isDeletedFileMode(String line, List<String> diffLines, int currentIndex) {
        return line.startsWith("deleted file mode") && diffLines.get(currentIndex + 1).startsWith("--- a/");
    }

    private static boolean isModifyFile(String line, List<String> diffLines, int currentIndex) {
        if (line.startsWith("---") || line.startsWith("+++")) {
            String nextLine = diffLines.get(currentIndex + 1);
            if (nextLine.startsWith("+++")) {
                String filePath = extractFilePath(nextLine);
                return !filePath.isEmpty();
            }
        }
        return false;
    }

    private static String extractFilePath(String line) {
        int revisionIndex = line.indexOf("(revision");
        if (revisionIndex != -1) {
            return line.substring("+++ b/".length(), revisionIndex).trim();
        }
        return line.substring("+++ b/".length()).trim();
    }
}
