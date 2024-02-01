package com.example.demo.filter;

import com.example.demo.renderer.ErrorIconRenderer;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.JvmExceptionOccurrenceFilter;
import com.intellij.execution.impl.InlayProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MyDebuggerFilter implements JvmExceptionOccurrenceFilter {

    public MyDebuggerFilter() {
    }

    @Override
    public Filter.Result applyFilter(@NotNull String exceptionClassName, @NotNull List<PsiClass> classes, int exceptionStartOffset) {
        return new CreateExceptionBreakpointResult(exceptionStartOffset, exceptionStartOffset + exceptionClassName.length(), exceptionClassName, ((PsiClass) classes.get(0)).getProject());
    }

    private static class CreateExceptionBreakpointResult extends Filter.Result implements InlayProvider {
        private final String myExceptionClassName;
        private final Project project;
        private final int startOffset;

        CreateExceptionBreakpointResult(int highlightStartOffset, int highlightEndOffset, String exceptionClassName, Project project) {
            super(highlightStartOffset, highlightEndOffset, null);
            this.myExceptionClassName = exceptionClassName;
            this.project = project;
            this.startOffset = highlightStartOffset;
        }

        public EditorCustomElementRenderer createInlayRenderer(Editor editor) {
            return new ErrorIconRenderer(editor, this.project, this.startOffset);
        }
    }
}
