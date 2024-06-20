package com.example.demo.utils.maven;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.LineSeparator;
import com.jediterm.terminal.Terminal;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AixCmdUtil {


    /**
     * 创建一个命令窗口
     */
    @SuppressWarnings("all")
    public static ShellTerminalWidget createTerminalWidget(Project project, String name) {
        AtomicReference<ShellTerminalWidget> terminalWidget = new AtomicReference<>();
        ToolWindow terminalToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
        if (terminalToolWindow == null) {
            throw new IllegalStateException("Terminal tool window is not available");
        }

        ApplicationManager.getApplication().invokeAndWait(() -> {
            TerminalView terminalView = TerminalView.getInstance(project);
            terminalWidget.set(terminalView.createLocalShellWidget(project.getBasePath(), name));
        });
        return terminalWidget.get();
    }

    @SuppressWarnings("all")
    public static FileWriter startTailF(Project project, ShellTerminalWidget terminalWidget) {
        // 创建临时脚本文件
        File scriptFile;
        try {
            scriptFile = File.createTempFile("tempLog", ".log");
            terminalWidget.executeCommand("tail -f " + scriptFile.getPath());
            return new FileWriter(scriptFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp script file", e);
        }
    }


    @SuppressWarnings("all")
    public static void runCommandByTempFile(Project project, ShellTerminalWidget terminalWidget, String command) {
        // 创建临时脚本文件
        File scriptFile;
        try {
            scriptFile = File.createTempFile("tempScript", ".sh");
            FileWriter writer = new FileWriter(scriptFile);
            writer.write("#!/bin/sh\n" + command + "\n");
            writer.close();
            scriptFile.setExecutable(true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp script file", e);
        }

        // 执行临时脚本文件
        try {
            terminalWidget.executeCommand(scriptFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String END = "=====================END=====================";

    @SuppressWarnings("all")
    public static void runCommandByTempFile(Project project, String command, Consumer<ShellTerminalWidget> onEnd) {
        // 创建临时脚本文件
        File scriptFile;
        try {
            scriptFile = File.createTempFile("tempScript", ".sh");
            FileWriter writer = new FileWriter(scriptFile);
            writer.write("#!/bin/sh\n" +
                    "echo =====================START=====================\n" +
                    command + "\n" +
                    "echo " + END + "\n"
            );
            writer.close();
            scriptFile.setExecutable(true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp script file", e);
        }

        // 获取 Terminal 工具窗口
        final ToolWindow terminalToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
        if (terminalToolWindow == null) {
            throw new IllegalStateException("Terminal tool window is not available");
        }

        // 激活 Terminal 工具窗口
        terminalToolWindow.activate(() -> {
            final TerminalView terminalView = TerminalView.getInstance(project);
            // 创建一个新的本地 Shell 终端小部件
            final ShellTerminalWidget terminalWidget = terminalView.createLocalShellWidget(project.getBasePath(), "AixTerminal");
            // 执行临时脚本文件
            try {
                terminalWidget.executeCommand(scriptFile.getAbsolutePath());
                new Thread(() -> {
                    long s = System.currentTimeMillis();
                    while (System.currentTimeMillis() - s < 300000) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                        final String text = terminalWidget.getText();
                        final String[] split = text.split("\n");
                        if (split.length > 1) {
                            final String lastEchoLine = split[split.length - 2];
                            if (END.equals(lastEchoLine)) {
                                if (onEnd != null) {
                                    onEnd.accept(terminalWidget);
                                }
                                break;
                            }
                        }
                    }
                }).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void writePlainMessage(ShellTerminalWidget terminalWidget, String message) {
        final Terminal terminal = terminalWidget.getTerminal();
        if (message == null) {
            return;
        }
        String str = StringUtil.convertLineSeparators(message, LineSeparator.LF.getSeparatorString());
        List<String> lines = StringUtil.split(str, LineSeparator.LF.getSeparatorString(), true, false);
        boolean first = true;
        for (Iterator<String> var5 = lines.iterator(); var5.hasNext(); first = false) {
            String line = (String) var5.next();
            if (!first) {
                terminal.carriageReturn();
                terminal.newLine();
            }
            terminal.writeCharacters(line);
        }
    }

    @SuppressWarnings("all")
    public static void runCommandByTempFileWait(Project project, String command) {
        // 创建临时脚本文件
        File scriptFile;
        try {
            scriptFile = File.createTempFile("tempScript", ".sh");
            try (FileWriter writer = new FileWriter(scriptFile)) {
                writer.write("#!/bin/sh\n" + command + "\n");
            }
            scriptFile.setExecutable(true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp script file", e);
        }

        // 获取 Terminal 工具窗口
        final ToolWindow terminalToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
        if (terminalToolWindow == null) {
            throw new IllegalStateException("Terminal tool window is not available");
        }

        // 激活 Terminal 工具窗口
        terminalToolWindow.activate(() -> {
            final TerminalView terminalView = TerminalView.getInstance(project);
            // 创建一个新的本地 Shell 终端小部件
            final ShellTerminalWidget terminalWidget = terminalView.createLocalShellWidget(project.getBasePath(), "AixTerminal");

            // 执行临时脚本文件
            try {
                terminalWidget.executeCommand(scriptFile.getAbsolutePath());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("all")
    public static void runCommand(Project project, List<String> commands) {
        final String command = commands.stream().reduce((a, b) -> a + "\n" + b).orElse("");
        runCommandByTempFile(project, command, null);
    }

    @SuppressWarnings("all")
    public static void runCommand(Project project, String command) {
        // 获取 Terminal 工具窗口
        final ToolWindow terminalToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
        if (terminalToolWindow == null) {
            throw new IllegalStateException("Terminal tool window is not available");
        }

        // 激活 Terminal 工具窗口
        terminalToolWindow.activate(() -> {
            final TerminalView terminalView = TerminalView.getInstance(project);
            // 创建一个新的本地 Shell 终端小部件
            ShellTerminalWidget terminalWidget = terminalView.createLocalShellWidget(project.getBasePath(), "AixTerminal");
            // 执行命令
            try {
                terminalWidget.executeCommand(command);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
