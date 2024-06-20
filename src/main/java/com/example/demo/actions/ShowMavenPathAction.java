package com.example.demo.actions;

import com.example.demo.utils.coverage.jacoco.AixJacocoExecListener;
import com.example.demo.utils.coverage.jacoco.JacocoGradleRunner;
import com.example.demo.utils.coverage.selected.SelectInfo;
import com.example.demo.utils.coverage.selected.SelectInfoJava;
import com.example.demo.utils.maven.AixMavenUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class ShowMavenPathAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 通过e 拿到Editor。
        final Editor srcEditor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        if (srcEditor == null) {
            return;
        }
        final Document srcDoc = srcEditor.getDocument();
        final VirtualFile srcVF = FileDocumentManager.getInstance().getFile(srcDoc);
        if (srcVF == null) {
            return;
        }
        // 拿到选择范围
        final SelectInfoJava selectInfo = new SelectInfoJava(srcEditor);
        final String srcPackageName = srcDoc.getText().split("\n")[0].replace("package ", "").replace(";", "").trim();
        final String srcClassName = srcVF.getNameWithoutExtension();

        // 拿到测试文件Editor
        final Editor posEditor = getTestEditor(srcEditor);
        if (posEditor == null) {
            return;
        }
        final Document posDoc = posEditor.getDocument();
        final VirtualFile posVF = FileDocumentManager.getInstance().getFile(posDoc);
        final String posPackageName = posDoc.getText().split("\n")[0].replace("package ", "").replace(";", "").trim();
        if (posVF == null) {
            return;
        }
        final String posClassName = posVF.getNameWithoutExtension();
        final String dTest = posPackageName + "." + posClassName;

        BuildEnv buildEnv = getBuildEnv(project);
        switch (buildEnv) {
            case MAVEN:
                runByMaven(project, srcClassName, dTest, srcPackageName, selectInfo);
                return;
            case GRADLE:
                // 传递 JaCoCo 插件和参数
                JacocoGradleRunner.runByGradle(project, srcClassName, dTest, srcPackageName, selectInfo);
                return;
            default:
        }
    }


    private enum BuildEnv {
        UNKNOWN,
        MAVEN,
        GRADLE,
    }

    private static BuildEnv getBuildEnv(Project project) {
        if (project.getBaseDir().findChild("pom.xml") != null) {
            return BuildEnv.MAVEN;
        } else if (project.getBaseDir().findChild("build.gradle") != null) {
            return BuildEnv.GRADLE;
        } else {
            return BuildEnv.UNKNOWN;
        }
    }

    private static void runByMaven(Project project, String srcClassName, String dTest, String srcPackageName, SelectInfo selectInfo) {
        // 传递 JaCoCo 插件和参数
        AixMavenUtil.runCMD(project, "AixCoverage-" + srcClassName, Arrays.asList(
                "clean",
                "org.jacoco:jacoco-maven-plugin:0.8.7:prepare-agent",
                "test",
                "org.jacoco:jacoco-maven-plugin:0.8.7:report",
                "-Dtest=" + dTest
        ), new AixJacocoExecListener(project, srcPackageName, srcClassName, selectInfo));
    }


    private Editor getTestEditor(Editor editorSrc) {
        final Project project = editorSrc.getProject();
        if (project == null) {
            return null;
        }
        final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        final VirtualFile vfSrc = fileDocumentManager.getFile(editorSrc.getDocument());
        if (vfSrc == null) {
            return null;
        }
        final String testFileName = vfSrc.getNameWithoutExtension() + "Test";
        // 获取FileEditorManager实例
        final FileEditorManager editorManager = FileEditorManager.getInstance(project);
        // 获取所有已经打开的文件
        final VirtualFile[] openFiles = editorManager.getOpenFiles();
        // 遍历每个打开的文件
        for (VirtualFile file : openFiles) {
            // 获取与文件关联的所有Editor对象
            final Document document = fileDocumentManager.getDocument(file);
            if (document == null) {
                continue;
            }
            final Editor[] editors = EditorFactory.getInstance().getEditors(document);
            for (Editor editor : editors) {
                if (file.getNameWithoutExtension().equals(testFileName)) {
                    final FileEditorManager fileEditorManager = FileEditorManager.getInstance(editorSrc.getProject());
                    fileEditorManager.openFile(file, true);
                    return editor;
                }
            }
        }
        return null;
    }

}
