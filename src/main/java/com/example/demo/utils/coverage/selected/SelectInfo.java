package com.example.demo.utils.coverage.selected;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import lombok.Data;

@Data
public class SelectInfo {
    private final Editor editor;
    private final int selectionStart;
    private final int selectionEnd;
    private final int startLine;
    private final int endLine;

    public SelectInfo(Editor editor) {
        this.editor = editor;
        SelectionModel selectionModel = editor.getSelectionModel();
        Document document = editor.getDocument();
        this.selectionStart = selectionModel.getSelectionStart();
        this.selectionEnd = selectionModel.getSelectionEnd();
        this.startLine = document.getLineNumber(selectionStart);
        this.endLine = document.getLineNumber(selectionEnd);
    }

    public boolean containsLineIndex(int lineIndex) {
        return lineIndex >= startLine && lineIndex <= endLine;
    }

    public boolean containsLineNum(int lineNum) {
        return containsLineIndex(lineNum - 1);
    }
}
