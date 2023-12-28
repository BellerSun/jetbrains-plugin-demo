package com.example.demo.frames;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class InputLenLimitFrame {
    private JPanel scrollPanel;
    private JPanel root;
    private JPanel monitorPanel;
    private JTextField monitorText;

    private final static int MAX_COMMENT_CHARS = 20;

    public InputLenLimitFrame() {
        final JTextArea comment = new JTextArea();

        // limit length
        final DefaultStyledDocument doc = new DefaultStyledDocument();
        comment.setDocument(doc);
        doc.setDocumentFilter(new DocumentSizeFilter(MAX_COMMENT_CHARS));

        // scroll
        final JScrollPane scroll = new JBScrollPane(comment);
        scrollPanel.setLayout(new GridLayout(1, 1));
        scrollPanel.add(scroll);


        // refresh content length
        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                monitorText.setText(doc.getLength() + "/" + MAX_COMMENT_CHARS);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                monitorText.setText(doc.getLength() + "/" + MAX_COMMENT_CHARS);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                monitorText.setText(doc.getLength() + "/" + MAX_COMMENT_CHARS);
            }
        });
        monitorText.setText(doc.getLength() + "/" + MAX_COMMENT_CHARS);
    }

    public JPanel getRoot() {
        return root;
    }

    private static class DocumentSizeFilter extends DocumentFilter {
        int maxCharacters;

        public DocumentSizeFilter(int maxChars) {
            maxCharacters = maxChars;
        }

        @Override
        public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
            final int protoLen = fb.getDocument().getLength();
            if ((protoLen + str.length()) <= maxCharacters) {
                super.insertString(fb, offs, str, a);
            } else {
                final String newStr = str.substring(0, maxCharacters - protoLen);
                super.insertString(fb, offs, newStr, a);
                Toolkit.getDefaultToolkit().beep();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a)
                throws BadLocationException {
            if ((fb.getDocument().getLength() + str.length() - length) <= maxCharacters) {
                super.replace(fb, offs, length, str, a);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}
