package com.example.demo.startup;

import com.example.demo.listener.MyProjectTaskListener;
import com.intellij.openapi.project.Project;
import com.intellij.task.ProjectTaskListener;
import com.intellij.util.messages.MessageBusConnection;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyPluginInitializer implements com.intellij.openapi.startup.ProjectActivity {


    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        MessageBusConnection myConn
                = project.getMessageBus().connect();
        myConn.subscribe(ProjectTaskListener.TOPIC, new MyProjectTaskListener());
        return project;
    }
}
