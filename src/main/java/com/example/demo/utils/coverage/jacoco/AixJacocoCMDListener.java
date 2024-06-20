package com.example.demo.utils.coverage.jacoco;

import com.example.demo.utils.coverage.jacoco.model.xml.*;
import com.example.demo.utils.coverage.selected.SelectInfo;
import com.example.demo.utils.maven.AixCmdUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;

import java.util.List;

public class AixJacocoCMDListener {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Project project;
    private final String workingDirectory;
    private final String packageName;
    private final String className;
    private final SelectInfo selectInfo;
    private final ShellTerminalWidget terminalWidget;

    public AixJacocoCMDListener(Project project, String packageName, String className, SelectInfo selectInfo, ShellTerminalWidget terminalWidget) {
        this.project = project;
        this.workingDirectory = project.getBasePath();
        this.packageName = packageName;
        this.className = className;
        this.selectInfo = selectInfo;
        this.terminalWidget = terminalWidget;
    }


    public void onProcessExit() {
        final JacocoReport jacocoReport = ApplicationManager.getApplication().runReadAction(
                (Computable<JacocoReport>) () -> JacocoAnalyzer.analyzeReport(workingDirectory + "/build/reports/jacoco/test/jacocoTestReport.xml"));
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
            final boolean containsLine = selectInfo.containsLineNum(method.getLine());
            if (!containsLine) {
                continue;
            }
            if (!methodName.matches("\\w+")) {
                continue;
            }

            final String printContent = calcPrintContent(method);

            AixCmdUtil.writePlainMessage(terminalWidget, printContent);
        }
    }


    /**
     * 计算输出文案
     */
    private static String calcPrintContent(JacocoMethod method) {
        final JacocoCounter branchCounter = method.getBranchCounter();
        final JacocoCounter lineCounter = method.getLineCounter();
        final int branchCovered = branchCounter.getCovered();
        final int branchTotal = branchCounter.getTotal();
        final float branchCoverageRate = (branchCovered == 0 && branchTotal == 0) ? 1 : branchCounter.getCoverageRate();
        final int lineCovered = lineCounter.getCovered();
        final int lineTotal = lineCounter.getTotal();
        final float lineCoverageRate = (lineCovered == 0 && lineTotal == 0) ? 1 : lineCounter.getCoverageRate();
        return String.format("\nhas passed testing with a line coverage of %.0f%% and branch coverage of %.0f%%.\n", lineCoverageRate * 100, branchCoverageRate * 100);
    }
}
