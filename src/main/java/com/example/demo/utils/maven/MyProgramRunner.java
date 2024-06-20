package com.example.demo.utils.maven;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.*;
import com.intellij.execution.runners.*;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyProgramRunner extends DefaultProgramRunner {
    @NotNull
    @Override
    public String getRunnerId() {
        return "MyProgramRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return true; // or your own logic
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        super.execute(environment);
        addProcessListener(environment);
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment, @Nullable ProgramRunner.Callback callback) throws ExecutionException {
        super.execute(environment, callback);
        addProcessListener(environment);
    }

    private void addProcessListener(@NotNull ExecutionEnvironment environment) {
        RunContentDescriptor descriptor = environment.getContentToReuse();
        if (descriptor != null) {
            ProcessHandler processHandler = descriptor.getProcessHandler();
            if (processHandler != null) {
                processHandler.addProcessListener(new ProcessAdapter() {
                    @Override
                    public void processTerminated(@NotNull ProcessEvent event) {
                        super.processTerminated(event);
                        // Calculate your banner here
                        String banner = "Your banner";
                        // Print the banner to the console
                        processHandler.notifyTextAvailable(banner, ProcessOutputTypes.STDOUT);
                    }
                });
            }
        }
    }
}