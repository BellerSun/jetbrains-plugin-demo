package com.example.demo.utils.maven;

import com.example.demo.utils.AixExecutionListenerDispatcher;
import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;

import java.util.List;

public class AixMavenUtil {

    public static void runCMD(Project project, String taskShowName, List<String> goals, ExecutionListener executionListener) {
        final MavenRunnerParameters params = new MavenRunnerParameters(
                true,
                project.getBasePath(),
                null,
                goals,
                null,
                null
        );

        final MavenRunConfigurationType configurationType = MavenRunConfigurationType.getInstance();
        final RunnerAndConfigurationSettings settings = RunManager.getInstance(project)
                .createConfiguration(taskShowName, configurationType.getConfigurationFactories()[0]);

        final MavenRunConfiguration configuration = (MavenRunConfiguration) settings.getConfiguration();
        configuration.setRunnerParameters(params);

        final ProgramRunner<?> runner = ProgramRunner.getRunner(DefaultRunExecutor.EXECUTOR_ID, configuration);
        if (runner != null) {
            try {
                final Executor executor = DefaultRunExecutor.getRunExecutorInstance();
                final ExecutionEnvironment environment = new ExecutionEnvironment(executor, runner, settings, project);
                if (null != executionListener) {
                    // 如果该任务配置了监听器，把配置的监听器添加到我们的分发器中
                    AixExecutionListenerDispatcher.getInstance(project).addListener(executionListener, environment);
                }
                runner.execute(environment);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
