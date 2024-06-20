package com.example.demo.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行监听器分发器
 */
public class AixExecutionListenerDispatcher implements ExecutionListener {
    //单例Map，每个项目一个实例。
    private static final Map<Project, AixExecutionListenerDispatcher> instanceMap = new ConcurrentHashMap<>();

    public static AixExecutionListenerDispatcher getInstance(Project project) {
        return instanceMap.computeIfAbsent(project, AixExecutionListenerDispatcher::new);
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Project project;

    private AixExecutionListenerDispatcher(Project project) {
        this.project = project;
        project.getMessageBus().connect().subscribe(ExecutionManager.EXECUTION_TOPIC, this);
    }

    /**
     * 用于存储执行ID和监听器的映射关系
     */
    private final BiMap<ExecutionEnvironment, ExecutionListener> dispatchMap = HashBiMap.create();

    public synchronized void addListener(ExecutionListener listener, ExecutionEnvironment env) {
        dispatchMap.put(env, listener);
    }

    private synchronized ExecutionListener chooseListener(ExecutionEnvironment env) {
        return dispatchMap.get(env);
    }

    private synchronized void removeListener(ExecutionEnvironment env) {
        dispatchMap.remove(env);
    }

    @Override
    public void processStartScheduled(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
        ExecutionListener executionListener = chooseListener(env);
        if (null == executionListener) {
            return;
        }
        executionListener.processStartScheduled(executorId, env);
    }

    @Override
    public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
        ExecutionListener executionListener = chooseListener(env);
        if (null == executionListener) {
            return;
        }
        executionListener.processStarting(executorId, env);
    }

    @Override
    public void processNotStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
        ExecutionListener executionListener = chooseListener(env);
        if (null == executionListener) {
            return;
        }
        executionListener.processNotStarted(executorId, env);
    }

    @Override
    public void processNotStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, Throwable cause) {
        ExecutionListener executionListener = chooseListener(env);
        if (null == executionListener) {
            return;
        }
        executionListener.processNotStarted(executorId, env, cause);
    }

    @Override
    public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        ExecutionListener executionListener = chooseListener(env);
        if (null == executionListener) {
            return;
        }
        executionListener.processStarting(executorId, env, handler);
    }

    @Override
    public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        ExecutionListener executionListener = chooseListener(env);
        if (null == executionListener) {
            return;
        }
        executionListener.processStarted(executorId, env, handler);
    }

    @Override
    public void processTerminating(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        ExecutionListener executionListener = chooseListener(env);
        if (null == executionListener) {
            return;
        }
        executionListener.processTerminating(executorId, env, handler);
    }

    @Override
    public void processTerminated(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler, int exitCode) {
        ExecutionListener executionListener = chooseListener(env);
        if (null == executionListener) {
            return;
        }
        try {
            executionListener.processTerminated(executorId, env, handler, exitCode);
        } finally {
            // 程序结束后，移除监听器
            removeListener(env);
        }
    }
}
