package com.example.demo.listener;

import com.intellij.task.ProjectTaskContext;
import com.intellij.task.ProjectTaskListener;
import com.intellij.task.ProjectTaskManager;
import org.jetbrains.annotations.NotNull;

/**
 * 监控项目构建任务的生命周期
 */
public class MyProjectTaskListener implements ProjectTaskListener {

    @Override
    public void started(@NotNull ProjectTaskContext context) {
        // 构建开始时的逻辑
        System.out.println("构建已开始");
    }

    @Override
    public void finished(ProjectTaskManager.@NotNull Result result) {

        if (result.isAborted()) {
            System.out.println("构建被中止");
        } else if (result.hasErrors()) {
            // 处理编译错误信息
            System.out.println("构建完成，发现错误");
            // 您可以在这里获取更详细的错误信息
        } else {
            System.out.println("构建成功，没有发现错误");
        }
    }
}

