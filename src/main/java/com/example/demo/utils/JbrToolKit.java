package com.example.demo.utils;

import com.intellij.openapi.util.TextRange;

import java.util.function.Predicate;

/**
 * Jetbrains平台工具包
 */
public final class JbrToolKit {
    private JbrToolKit() {
    }


    /**
     * Document相关工具
     */
    public static final class Document {
        private Document() {
        }

        /**
         * 获取某行的内容文本
         */
        public static String getLine(com.intellij.openapi.editor.Document document, int line) {
            return document.getText(new TextRange(document.getLineStartOffset(line), document.getLineEndOffset(line)));
        }

        /**
         * 获取多行的内容文本
         */
        public static String getLines(com.intellij.openapi.editor.Document document, int startLine, Predicate<String> cutPredicate) {
            return getLines(document, startLine, document.getLineCount(), cutPredicate);
        }

        /**
         * 获取多行的内容文本
         *
         * @param document     文档
         * @param startLine    开始行
         * @param endLine      结束行
         * @param cutPredicate 截断条件
         * @return 文本
         */
        public static String getLines(com.intellij.openapi.editor.Document document, int startLine, int endLine, Predicate<String> cutPredicate) {
            final StringBuilder sb = new StringBuilder();
            for (int i = startLine; i < endLine; i++) {
                final String line = getLine(document, i);
                if (cutPredicate.test(line)) {
                    // 截断
                    break;
                }
                sb.append("\n").append(line);
            }
            return sb.toString();
        }
    }
}
