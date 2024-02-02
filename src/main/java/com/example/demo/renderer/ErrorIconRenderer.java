package com.example.demo.renderer;

import cn.hutool.core.text.TextSimilarity;
import com.example.demo.utils.JbrToolKit;
import com.intellij.codeInsight.hints.presentation.InputHandler;
import com.intellij.execution.filters.ExceptionWorker;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import icons.MyIcons;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ErrorIconRenderer implements EditorCustomElementRenderer, InputHandler {
    private static final Set<String> ERROR_INFO_SIGN = Set.of("at ", "Caused by", "...");
    private final Editor editor;
    private final Project myProject;
    private final int startOffset;

    public ErrorIconRenderer(Editor editor, Project project, int starOffset) {
        this.editor = editor;
        this.myProject = project;
        this.startOffset = starOffset;
    }


    @Override
    public void mouseExited() {
        ((EditorImpl) this.editor).setCustomCursor(this, Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    @Override
    public void mouseMoved(@NotNull MouseEvent mouseEvent, @NotNull Point point) {
        ((EditorImpl) this.editor).setCustomCursor(this, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        return MyIcons.stocking.getIconWidth();
    }

    @Override
    public int calcHeightInPixels(@NotNull Inlay inlay) {
        return MyIcons.stocking.getIconHeight();
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
        Icon consoleIcon = MyIcons.stocking;
        int curX = r.x + r.width / 2 - consoleIcon.getIconWidth() / 2;
        int curY = r.y + r.height / 2 - consoleIcon.getIconHeight() / 2;
        if (curX >= 0 && curY >= 0) {
            consoleIcon.paintIcon(inlay.getEditor().getComponent(), g, curX, curY);
        }
    }


    private String analysisErrorContent(Document document, int line) {
        final String head = JbrToolKit.Document.getLine(document, line);
        final String lines = JbrToolKit.Document.getLines(document, ++line, lineStr -> ERROR_INFO_SIGN.stream().allMatch(lineStr::contains));
        return head + lines;
    }


    @Override
    public void mouseClicked(@NotNull MouseEvent mouseEvent, @NotNull Point point) {
        System.out.println("mouseClicked");

        // 获取过滤到信息开始,是原文的第几行
        int line = this.editor.getDocument().getLineNumber(this.startOffset);
        // 拿到错误文本信息(准备给算法参考)
        final String errorContent = this.analysisErrorContent(this.editor.getDocument(), line);
        System.out.println("errorContent");
        System.out.println(errorContent);

        String errorLineContent = this.findErrorLineContent(myProject, editor, line);
        System.out.println("errorLineContent");
        System.out.println(errorLineContent);
    }

    public String findErrorLineContent(Project project, Editor errEditor, int line) {
        final Document errDoc = errEditor.getDocument();
        while (line < errDoc.getLineCount()) {
            final String lineContent = JbrToolKit.Document.getLine(errDoc, line);
            final ExceptionWorker.ParsedLine parsedExptLine = ExceptionWorker.parseExceptionLine(lineContent);
            final java.util.List<VirtualFile> virtualFiles;
            final VirtualFile userVf;
            if (parsedExptLine == null
                    || parsedExptLine.fileName == null
                    || CollectionUtils.isEmpty(virtualFiles = new ArrayList<>(FilenameIndex.getVirtualFilesByName(parsedExptLine.fileName, GlobalSearchScope.projectScope(project))))
                    || null == (userVf = findVF(virtualFiles, lineContent.substring(parsedExptLine.classFqnRange.getStartOffset(), parsedExptLine.classFqnRange.getEndOffset())))
            ) {
                line++;
                continue;
            }
            try {
                final String codeBlockContent = analysisCodeBlock(userVf, parsedExptLine.lineNumber);
                final Language language = com.intellij.lang.LanguageUtil.getFileLanguage(userVf);
                final String languageStr = language != null ? language.getDisplayName().toLowerCase() : null;

                return "```" +
                        StringUtils.defaultIfBlank(languageStr, "") +
                        "\n" +
                        codeBlockContent +
                        "\n```\n";
            } catch (Exception ignored) {
            } finally {
                line++;
            }
        }
        return null;
    }

    public VirtualFile findVF(List<VirtualFile> virtualFiles, String classFullPath) {
        if (CollectionUtils.isEmpty(virtualFiles) || classFullPath == null) {
            return null;
        }
        // 拿到代码所在目录
        final String logDir = classFullPath.substring(0, classFullPath.lastIndexOf(".")).replace(".", "/");
        final String pathAll = virtualFiles.get(0).getPath();
        final String srcDirPath = pathAll.substring(0, pathAll.indexOf(logDir));

        // 构建map, 把vf的路径格式转换为classFullPath的格式(代码路径开头,然后还没有后缀名)
        final Map<String, VirtualFile> vfFullPathMap = virtualFiles.stream().collect(Collectors.toMap(vf -> {
            final String pathHasSuffix = vf.getPath().replace(srcDirPath, "").replace("/", ".");
            return pathHasSuffix.substring(0, pathHasSuffix.lastIndexOf("."));
        }, vf -> vf));
        // 如果精确匹配到了,直接返回
        final VirtualFile matchVf = vfFullPathMap.get(classFullPath);
        if (null != matchVf) {
            return matchVf;
        }

        // 精确匹配失败,尝试模糊匹配
        return vfFullPathMap.keySet().stream().max(Comparator.comparingDouble(vfFullPath -> TextSimilarity.similar(classFullPath, vfFullPath))).map(vfFullPathMap::get).orElse(null);
    }

    /**
     * 找到所在行的那个方法,然后返回该方法文本
     *
     * @param vf      文件
     * @param lineNum 要找的内容行
     * @return 如果找到了代码块，返回其文本内容；否则返回空文本
     */
    public String analysisCodeBlock(VirtualFile vf, int lineNum) {
        final Document userDoc = FileDocumentManager.getInstance().getDocument(vf);
        if (null == userDoc) {
            return "";
        }
        final PsiFile psiFile = PsiManager.getInstance(this.myProject).findFile(vf);
        if (psiFile == null) {
            return "";
        }
        // 在给定行找到PSI元素
        PsiElement element = psiFile.findElementAt(userDoc.getLineStartOffset(lineNum));
        // 向上层找,一直找到方法或者类
        while (element != null && !(element instanceof PsiMethod || element instanceof PsiClass)) {
            element = element.getParent();
        }
        // 如果找到了块，返回其文本
        if (element != null) {
            return element.getText();
        }
        // 如果没有找到块，返回空
        return "";
    }
}
