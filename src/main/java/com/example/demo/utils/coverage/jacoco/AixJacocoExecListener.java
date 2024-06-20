package com.example.demo.utils.coverage.jacoco;

import com.example.demo.utils.coverage.jacoco.model.xml.*;
import com.example.demo.utils.coverage.selected.SelectInfo;
import com.example.demo.utils.coverage.selected.SelectInfoJava;
import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@Slf4j
public class AixJacocoExecListener implements ExecutionListener {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Project project;
    private final String workingDirectory;
    private final String packageName;
    private final String className;
    private final SelectInfo selectInfo;

    public AixJacocoExecListener(Project project, String packageName, String className, SelectInfo selectInfo) {
        this.project = project;
        this.workingDirectory = project.getBasePath();
        this.packageName = packageName;
        this.className = className;
        this.selectInfo = selectInfo;
    }

    /**
     * 在进程退出之前计算我们的逻辑
     */
    @Override
    public void processTerminating(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        try {
            processTerminatingCustom(executorId, env, handler);
        } finally {
            // 最后继续运行程序
            ExecutionListener.super.processTerminating(executorId, env, handler);
        }
    }

    private void processTerminatingCustom(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        final ConsoleView consoleView = (ConsoleView) Optional.of(env)
                .map(ExecutionEnvironment::getContentToReuse)
                .map(RunContentDescriptor::getExecutionConsole)
                .filter(o -> o instanceof ConsoleView)
                .orElse(null);
        if (null == consoleView) {
            log.info("[JACOCO_EXEC_LISTENER] consoleView is null");
        }

        final JacocoReport jacocoReport = ApplicationManager.getApplication().runReadAction(
                (Computable<JacocoReport>) () -> JacocoAnalyzer.analyzeReport(workingDirectory + "/target/site/jacoco/jacoco.xml"));
        // 通过执行的console拿到输出的内容
        final JacocoPackage pkgExample = jacocoReport.getJacocoPackage(packageName);
        if (null == pkgExample) {
            return;
        }
        final JacocoClass classFactorial = pkgExample.getJacocoClass(packageName, className);
        if (null == classFactorial) {
            return;
        }

        final List<JacocoMethod> methods = classFactorial.getMethods();
        for (JacocoMethod method : methods) {
            final String methodName = method.getName();
            final String methodDesc = method.getDesc();
            final boolean containsLine = selectInfo.containsLineNum(method.getLine());
            if (!containsLine) {
                continue;
            }
            if (!methodName.matches("\\w+")) {
                continue;
            }
            JacocoCounter branchCounter = method.getBranchCounter();
            JacocoCounter lineCounter = method.getLineCounter();
            JacocoCounter complexityCounter = method.getComplexityCounter();
            JacocoCounter instructionCounter = method.getInstructionCounter();
            //todo  进一步确认，这个line计算的方式稳不稳？内置的构造函数之类的咋整？wait之类的父类方法咋整？testng没有构造函数junit有构造函数咋整？
            consoleView.print("-------------------------------------------\n", ConsoleViewContentType.NORMAL_OUTPUT);
            consoleView.print("方法：" + methodName + methodDesc + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
            consoleView.print("分支覆盖率：" + branchCounter.getCovered() + "/" + branchCounter.getTotal() + "=" + branchCounter.getCoverageRate() + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
            consoleView.print("行覆盖率：" + lineCounter.getCovered() + "/" + lineCounter.getTotal() + "=" + lineCounter.getCoverageRate() + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
            consoleView.print("复杂度：" + complexityCounter.getCovered() + "/" + complexityCounter.getTotal() + "=" + complexityCounter.getCoverageRate() + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
            consoleView.print("指令覆盖率：" + instructionCounter.getCovered() + "/" + instructionCounter.getTotal() + "=" + instructionCounter.getCoverageRate() + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
            consoleView.print("-------------------------------------------\n", ConsoleViewContentType.NORMAL_OUTPUT);
        }
    }
}
