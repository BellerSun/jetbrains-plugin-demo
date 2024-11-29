package com.aixcoder.jbpadapter;

import com.aixcoder.jbpadapter.enums.UsedType;
import com.aixcoder.jbpadapter.model.UsedIdentifier;
import com.aixcoder.jbpadapter.utils.AixPsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.jetbrains.cidr.lang.psi.*;
import com.jetbrains.cidr.lang.psi.impl.OCQualifiedExpressionImpl;
import com.jetbrains.cidr.lang.psi.visitors.OCRecursiveVisitor;
import com.jetbrains.cidr.lang.symbols.OCSymbol;
import com.jetbrains.cidr.lang.symbols.OCSymbolKind;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class CPsiTool extends AixPsiTool<OCFunctionDefinition> {

    @Override
    public List<OCFunctionDefinition> getUsedMethods(Project project, PsiElement method) {
        final List<UsedIdentifier> usedDefinitions = this.getUsedDefinitionsByType(project, method, UsedType.METHOD);
        return usedDefinitions.stream().map(UsedIdentifier::getPsiElemTo)
                .filter(e -> e instanceof OCFunctionDefinition)
                .map(e -> (OCFunctionDefinition) e)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTestCase(OCFunctionDefinition definition) {
        return "TestBody".equals(definition.getName()) && definition.getDeclarator() != null && definition.getDeclarator().getText().isEmpty();
    }

    @Override
    public String getMethodSignature(OCFunctionDefinition definition) {
        final String[] lines = definition.getText().split("\n");
        return definition.getDeclarator() == null ? (lines.length > 0 ? lines[0] : definition.getText()) : definition.getDeclarator().getText();
    }

    @Override
    public List<UsedIdentifier> getUsedDefinitions(Project project, PsiElement elem) {
        return this.getUsedDefinitionsByType(project, elem);
    }

    private List<UsedIdentifier> getUsedDefinitionsByType(Project project, PsiElement elem, UsedType... usedTypes) {
        final boolean needFilterType = usedTypes != null && usedTypes.length > 0;
        final Set<UsedType> usedTypeSet = Arrays.stream(usedTypes == null ? new UsedType[0] : usedTypes).collect(Collectors.toSet());
        final LinkedBlockingQueue<UsedIdentifier> queue = new LinkedBlockingQueue<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            // 遍历 PSI 树
            elem.accept(new OCRecursiveVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    super.visitElement(element);
                    if (element.getChildren().length != 0
                        // 表达式的，不是叶子结点也没关系
                        && !(element instanceof OCQualifiedExpressionImpl)) {
                        return;
                    }
                    // 获取元素的所有引用
                    PsiReference[] references = element.getReferences();
                    for (PsiReference reference : references) {
                        PsiElement resolved = reference.resolve();
                        final PsiFile containingFile;
                        if (resolved == null
                            // 确保目标是文件
                            || (containingFile = resolved.getContainingFile()) == null
                            // 确保文件在当前项目中
                            || !ProjectRootManager.getInstance(project).getFileIndex().isInContent(containingFile.getVirtualFile())
                            // 不处理当前文件
                            || containingFile.equals(elem.getContainingFile())
                        ) {
                            continue;
                        }
                        // 避免重复打印
                        if (resolved instanceof OCSymbolDeclarator<?>) {
                            final OCSymbol symbol = ((OCSymbolDeclarator<?>) resolved).getSymbol();
                            final UsedType usedType = getUsedType(symbol);
                            if (needFilterType && !usedTypeSet.contains(usedType)) {
                                continue;
                            }
                            if (!isHeaderFile(containingFile)) {
                                // 普通文件，直接返回啦。
                                PsiElement identifier = findIdentifier(element);
                                UsedIdentifier usedIdentifier = new UsedIdentifier(identifier, usedType, resolved);
                                queue.add(usedIdentifier);
                                continue;
                            }
                            PsiFile implFile = getImplFile(containingFile.getParent(), containingFile.getName());
                            if (implFile == null) {
                                continue;
                            }
                            // 定位方法实现
                            PsiElement extracted = findImplPsi(implFile, resolved);
                            if (extracted != null) {
                                PsiElement identifier = findIdentifier(element);
                                UsedIdentifier usedIdentifier = new UsedIdentifier(identifier, usedType, extracted);
                                queue.add(usedIdentifier);
                            }
                        }
                    }
                }
            });
        });
        return new ArrayList<>(queue);
    }

    @Override
    public List<UsedIdentifier> getRefDefinitions(Project project, PsiElement elem) {
        PsiFile psiFile = elem.getContainingFile();
        final LinkedBlockingQueue<UsedIdentifier> queue = new LinkedBlockingQueue<>();

        ApplicationManager.getApplication().runReadAction(() -> {
            GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
            Query<PsiReference> references = ReferencesSearch.search(elem, searchScope);
            Collection<PsiReference> all = references.findAll();
            for (PsiReference reference : all) {
                ApplicationManager.getApplication().runReadAction(() -> {
                    PsiElement referencingElement = reference.getElement();
                    PsiFile referencingFile = referencingElement.getContainingFile();
                    if (referencingFile != null && !referencingFile.equals(psiFile)) {

                        PsiElement identifier = findIdentifier(referencingElement);
                        UsedIdentifier usedIdentifier = new UsedIdentifier(identifier, UsedType.METHOD, elem);
                        queue.add(usedIdentifier);
                    }
                });
            }
        });
        return new ArrayList<>(queue);
    }


    private static UsedType getUsedType(OCSymbol symbol) {
        if (symbol == null) {
            return UsedType.UNKNOWN;
        }
        final OCSymbolKind symbolKind = symbol.getKind();
        final String name = symbolKind.getName();
        switch (name) {
            case "Struct":
            case "Constructor":
                return UsedType.CLASS;
            case "Function":
                return UsedType.METHOD;
            default:
                return UsedType.UNKNOWN;
        }
    }

    private boolean isHeaderFile(PsiFile file) {
        // 检查文件是否为.h、.hpp、.hxx或.hh文件
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".h") || fileName.endsWith(".hpp") || fileName.endsWith(".hxx") || fileName.endsWith(".hh");
    }

    private PsiFile getImplFile(PsiDirectory directory, String headerFileName) {
        // 根据头文件名获取对应的实现文件名
        String baseName = headerFileName.substring(0, headerFileName.lastIndexOf('.'));
        List<String> possibleExtensions = Arrays.asList(".cpp", ".c", ".cc", ".cxx", ".cp");
        for (String ext : possibleExtensions) {
            String implementationFileName = baseName + ext;
            PsiFile psiFile = directory.findFile(implementationFileName);
            if (psiFile != null) {
                return psiFile;
            }
        }
        return null;
    }

    private PsiElement findImplPsi(PsiFile implFile, PsiElement resolved) {
        if (resolved instanceof OCDeclarator) {
            return PsiTreeUtil.findChildrenOfType(implFile, OCFunctionDefinition.class)
                    .stream()
                    .filter(functionDefinition -> isSameMethod((OCDeclarator) resolved, functionDefinition.getDeclarator()))
                    .findFirst()
                    .orElse(null);
        } else if (resolved instanceof OCStruct) {
            return PsiTreeUtil.findChildrenOfType(implFile, OCFunctionDefinition.class)
                    .stream()
                    .filter(functionDefinition -> isSameMethod((OCStruct) resolved, functionDefinition.getDeclarator()))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }


    private boolean isSameMethod(OCStruct headerStruct, OCDeclarator implMethod) {
        final List<OCDeclarator> list = headerStruct.getConstructors().stream().map(OCFunctionDeclaration::getDeclarator).collect(Collectors.toList());
        for (OCDeclarator declarator : list) {
            if (isSameMethod(declarator, implMethod)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameMethod(OCDeclarator headerMethod, OCDeclarator implMethod) {
        if (headerMethod == null || implMethod == null) {
            return false;
        }

        // 比较方法名
        String headerMethodName = headerMethod.getName();
        String implMethodName = implMethod.getName();

        if (!Objects.equals(headerMethodName, implMethodName)) {
            return false;
        }

        // 比较参数列表
        OCParameterList headerParams = headerMethod.getParameterList();
        OCParameterList implParams = implMethod.getParameterList();

        if (headerParams == null || implParams == null) {
            return false;
        }

        List<String> headerParamTypes = headerParams.getParameters().stream()
                .map(param -> param.getType().toString())
                .collect(Collectors.toList());

        List<String> implParamTypes = implParams.getParameters().stream()
                .map(param -> param.getType().toString())
                .collect(Collectors.toList());

        return headerParamTypes.equals(implParamTypes);
    }


    private PsiElement findIdentifier(PsiElement parent){
        for (@NotNull PsiElement child : parent.getChildren()) {
            String type = AixPsiUtils.getType(child);
            System.out.println(type + ":"+parent.getText());
        }
        return parent;
    }
}
