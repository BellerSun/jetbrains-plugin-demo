package com.example.demo.actions.psi;

import com.aixcoder.jbpadapter.CPsiTool;
import com.aixcoder.jbpadapter.enums.UsedType;
import com.aixcoder.jbpadapter.model.UsedIdentifier;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchRequestCollector;
import com.intellij.psi.search.SearchSession;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.jetbrains.cidr.lang.psi.OCDeclarator;
import com.jetbrains.cidr.lang.psi.OCFunctionDefinition;
import com.jetbrains.cidr.lang.psi.OCIncludeDirective;
import com.jetbrains.cidr.lang.psi.OCParameterList;
import com.jetbrains.cidr.lang.psi.impl.OCFileImpl;
import com.jetbrains.cidr.lang.types.OCType;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CActionFindRefOuter extends AnAction {
    private final ExecutorService executor = Executors.newCachedThreadPool(); // 线程池用于异步执行

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        executor.submit(() -> ApplicationManager.getApplication().runReadAction(() -> actionPerformed0(e)));
    }

    public void actionPerformed0(@NotNull AnActionEvent e) {
        Object data = e.getDataContext().getData("psi.File");
        if (!(data instanceof PsiFile)) {
            return;
        }
        PsiFile psiFile = (PsiFile) data;
        // 获取文件路径
        String path = psiFile.getVirtualFile().getPath();
        // 检查文件是否为 C/C++ 文件
        if (!(psiFile instanceof OCFileImpl)) {
            return;
        }
        OCFileImpl cFile = (OCFileImpl) psiFile;
        // 获取包含的头文件（include 语句）
        List<OCIncludeDirective> includes = cFile.findIncludeDirectives();
        for (OCIncludeDirective include : includes) {
            //System.out.println("[DEBUG] Include: " + include.getText());
        }


        CPsiTool cPsiTool = new CPsiTool();

        // 遍历 PSI 树并打印元素类型
        List<OCFunctionDefinition> methodList = PsiTreeUtil.findChildrenOfType(cFile, OCFunctionDefinition.class).stream().toList();
        String collect = methodList.stream().map(o -> o.getDeclarator().getText()).collect(Collectors.joining("\n"));
        for (OCFunctionDefinition function : methodList) {
            String functionName = function.getName();
            OCType returnType = function.getReturnType();
            //System.out.println("[DEBUG] Function: " + functionName + " (Return Type: " + returnType.getName() + ")");
            // 获取函数的参数列表
            OCParameterList parameterList = function.getParameterList();
            if (parameterList != null) {
                List<OCDeclarator> parameters = parameterList.getParameters();
                for (OCDeclarator parameter : parameters) {
                    String parameterName = parameter.getName();
                    OCType type = parameter.getType();
                   // System.out.println("[DEBUG] Parameter: " + parameterName + " (Type: " + type.getName() + ")");
                }
            }

            // 获取我依赖了谁
            ProgressManager.getInstance().run(new Task.Backgroundable(psiFile.getProject(), "正在查询我依赖了谁", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ApplicationManager.getApplication().runReadAction(() -> {
                        // 在 UI 线程中执行
                        List<UsedIdentifier> usedDefinitions = cPsiTool.getUsedDefinitions(psiFile.getProject(), function);
                        usedDefinitions.forEach(CActionFindRefOuter::logIdentifier);
                    });
                }
            });
            // 获取谁依赖了我

            ProgressManager.getInstance().run(new Task.Backgroundable(psiFile.getProject(), "正在查询谁依赖了我", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ApplicationManager.getApplication().runReadAction(() -> {
                        // 在 UI 线程中执行
                        //List<UsedIdentifier> refDefinitions = cPsiTool.getRefDefinitions(psiFile.getProject(), function);
                        //refDefinitions.forEach(CActionFindRefOuter::logIdentifier);
                        //findFileDependencies(psiFile, function, psiFile.getProject());
                    });
                }
            });
            String methodSignature = cPsiTool.getMethodSignature(function);
            boolean testCase = cPsiTool.isTestCase(function);
            System.out.println("[DEBUG] isTestCase: " + testCase+", method: " + methodSignature);
        }
        //System.out.println("[DEBUG] File Path: " + path);
        //System.out.println("[DEBUG] PsiFile Class: " + psiFile.getClass());
    }


    private static void logIdentifier(UsedIdentifier usedIdentifier) {
        PsiFile psiFileFrom = usedIdentifier.getPsiFileFrom();
        PsiElement psiElemFrom = usedIdentifier.getPsiElemFrom();
        PsiFile psiFileTo = usedIdentifier.getPsiFileTo();
        PsiElement psiElemTo = usedIdentifier.getPsiElemTo();
        UsedType usedType = usedIdentifier.getUsedType();

        System.out.println("[DEBUG] 引用信息[" + psiFileFrom.getName() + "][ " + psiElemFrom.getText() + psiElemFrom.getTextRange().toString() + "]==>[" + psiFileTo.getName() + "][" + psiElemTo.getText().toString() + "], 引用类型: " + usedType);
    }


    /**
     * 查询这个方法被哪些文件依赖了（类似用户 Ctrl+鼠标 点击方法名）
     */
    public void findFileDependencies(PsiFile psiFile, PsiElement element, Project project) {

        StopWatch watchGlobal = new StopWatch();
        watchGlobal.start();
        System.out.println("[DEBUG] --------------------start find who depend me--------------------");
        // 查询该元素的所有引用
        StopWatch watch = new StopWatch();
        watch.start();
        // 项目中这里一定要设置一个超时，大不俩就不用了，反正下次就好了。总之不能一直卡在这里
        GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
        Query<PsiReference> references = ReferencesSearch.search(element, searchScope);
        watch.stop();
        System.out.println("[DEBUG] 谁引用了我---查询引用耗时：" + watch.getTime() + "ms");
        watch.reset();
        watch.start();
        Collection<PsiReference> all = references.findAll();
        watch.stop();
        System.out.println("[DEBUG] 谁引用了我---查询引用findAll耗时：" + watch.getTime() + "ms");
        watch.reset();
        watch.start();

        int i = 0;

        StopWatch watchFor = new StopWatch();
        watchFor.start();
        for (PsiReference reference : all) {
            if (i == 0) {
                watchFor.stop();
                System.out.println("[DEBUG] 谁引用了我---第一次查询referencingElement耗时：" + watchFor.getTime() + "ms");
            }
            ++i;
            PsiElement referencingElement = reference.getElement();
            PsiFile referencingFile = referencingElement.getContainingFile();
            if (referencingFile != null && !referencingFile.equals(psiFile)) {
                VirtualFile vf = referencingFile.getVirtualFile();
                String referencingFilePath = vf.getPath();
                System.out.println("[DEBUG] 谁引用了我---这个文件依赖了我： " + referencingFilePath);
            }
        }
        watch.stop();
        System.out.println("[DEBUG] 谁引用了我---遍历次数" + i + ",耗时：" + watch.getTime() + "ms");

        watchGlobal.stop();
        System.out.println("[DEBUG] --------------------end find who depend me--------------------useTime:" + watchGlobal.getTime() + "ms");
    }


    /**
     * 估计只能用在java这种，方法头上面带有usage的。
     */
    public void findFileDependenciesOptimized(PsiFile psiFile, PsiElement method, Project project) {

        SearchRequestCollector requestCollector = new SearchRequestCollector(new SearchSession(method));

        ReferencesSearch.searchOptimized(method, GlobalSearchScope.projectScope(project), false, requestCollector, new Processor<PsiReference>() {
            @Override
            public boolean process(PsiReference reference) {
                PsiElement referencingElement = reference.getElement();
                PsiFile referencingFile = referencingElement.getContainingFile();
                if (referencingFile != null && !referencingFile.equals(psiFile)) {
                    VirtualFile vf = referencingFile.getVirtualFile();
                    String referencingFilePath = vf.getPath();
                    System.out.println("[DEBUG] 谁引用了我---这个文件依赖了我： " + referencingFilePath);
                }
                return true;
            }
        });

    }


}
