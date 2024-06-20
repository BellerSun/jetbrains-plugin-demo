package com.example.demo.utils.coverage.jacoco;

import com.example.demo.utils.coverage.selected.SelectInfo;
import com.example.demo.utils.maven.AixCmdUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JacocoGradleRunner {

    public static void runByGradle(Project project, String srcClassName, String dTest, String srcPackageName, SelectInfo selectInfo) {

        final List<String> cmdGradleList = Arrays.asList(
                "./gradlew",
                "clean",
                "-I",
                getTempGradleFile().getPath(),
                "test",
                "-Pplugins=org.jacoco:org.jacoco.core:0.8.12",
                "-PtestFinalizer=org.jacoco.gradle.JacocoPluginExtension",
                "-PtestFinalizerArgs='{\"report\":{\"xml\":{\"required\":true,\"enabled\":true},\"html\":{\"enabled\":true}}}'",
                "--tests",
                dTest,
                "--info"
        );
        final String cmdGradle = String.join(" ", cmdGradleList);
        final String cmdBanner = "echo \"====================Jacoco Gradle Runner====================\"";

        List<String> commands = Arrays.asList(cmdGradle, cmdBanner);
        AixCmdUtil.runCommandByTempFile(project, cmdGradle, terminalWidget -> new AixJacocoCMDListener(project, srcPackageName, srcClassName, selectInfo, terminalWidget).onProcessExit());
    }

    public static void runByGradleTailf(Project project, String srcClassName, String dTest, String srcPackageName, SelectInfo selectInfo) {
        if (null == project) {
            return;
        }

        final File scriptFile = getTempGradleFile();
        final List<String> cmdList = Arrays.asList(
                "./gradlew",
                "clean",
                "-I",
                getTempGradleFile().getPath(),
                "test",
                "-Pplugins=org.jacoco:org.jacoco.core:0.8.12",
                "-PtestFinalizer=org.jacoco.gradle.JacocoPluginExtension",
                "-PtestFinalizerArgs='{\"report\":{\"xml\":{\"required\":true,\"enabled\":true},\"html\":{\"enabled\":true}}}'",
                "--tests",
                dTest,
                "--info"
        );
        final ShellTerminalWidget terminalWidget = AixCmdUtil.createTerminalWidget(project, "AixCoverage-" + srcClassName);
        final FileWriter fileWriter = AixCmdUtil.startTailF(project, terminalWidget);
        try {
            // 创建一个ProcessBuilder实例，传入你想要运行的命令
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(cmdList);
            // 设置工作目录
            processBuilder.directory(new File(project.getBasePath()));
            // 启动一个新进程
            final Process process = processBuilder.start();
            process.onExit().whenComplete(((e, throwable) -> {
                new AixJacocoCMDListener(project, srcPackageName, srcClassName, selectInfo, terminalWidget).onProcessExit();
            }));
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                String line;
                while (true) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
                        if ((line = bufferedReader.readLine()) == null) break;
                        String content = line;
                        fileWriter.write(content + "\n");
                    } catch (IOException e) {
                    }
                }
            });
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                String line;
                while (true) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "utf-8"));
                        if ((line = bufferedReader.readLine()) == null) break;
                        String content = "[ERR]:" + line;
                        fileWriter.write(content + "\n");
                    } catch (IOException e) {
                    }
                }
            });
        } catch (Exception e) {
            log.error("runByGradle error", e);
        }
    }

    private static File scriptFile;

    private static synchronized File getTempGradleFile() {
        if (scriptFile != null) {
            return scriptFile;
        }
        try {
            scriptFile = File.createTempFile("init", ".gradle");
            final FileWriter writer = new FileWriter(scriptFile);
            writer.write("allprojects {\n" +
                    "    afterEvaluate {\n" +
                    "        tasks.test {\n" +
                    "            finalizedBy(tasks.jacocoTestReport)\n" +
                    "        }\n" +
                    "        tasks.jacocoTestReport {\n" +
                    "            dependsOn(tasks.test)\n" +
                    "            reports {\n" +
                    "                xml.required.set(true)\n" +
                    "                html.required.set(true)\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp script file", e);
        }
        return scriptFile;
    }

}
