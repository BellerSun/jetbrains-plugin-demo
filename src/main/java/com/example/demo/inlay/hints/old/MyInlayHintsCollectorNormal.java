package com.example.demo.inlay.hints.old;

import com.intellij.codeInsight.hints.InlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.InlayPresentationFactory;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.InsetPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.codeInsight.hints.presentation.SequencePresentation;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("UnstableApiUsage")
public class MyInlayHintsCollectorNormal implements InlayHintsCollector {

    private final Editor editor;

    public MyInlayHintsCollectorNormal(Editor editor) {
        this.editor = editor;
    }


    @Override
    public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
        PsiMethod[] methods = PsiTreeUtil.getChildrenOfType(psiElement, PsiMethod.class);
        if (methods != null) {
            for (PsiMethod method : methods) {
                addMethodInlayTipForMethod(editor, method, inlayHintsSink);
            }
        }
        return true;
    }


    AtomicInteger methodCnt = new AtomicInteger();

    private void addMethodInlayTipForMethod(Editor editor, PsiMethod method, InlayHintsSink inlayHintsSink) {
        int textOffset = method.getTextOffset();

        final boolean isEven = methodCnt.getAndIncrement() % 2 == 0;

        InlayPresentation presentation = isEven ? this.generateInlayPresentationInline(editor, method) : this.generateInlayPresentationDropdown(editor, method);
        inlayHintsSink.addBlockElement(textOffset, true, true, 1, presentation);
    }


    /**
     * 生成行内的提示,多个action会用|分隔
     */
    private InlayPresentation generateInlayPresentationInline(Editor editor, PsiMethod method) {
        final PresentationFactory factory = new PresentationFactory(editor);
        // 计算需要的左边距
        int leftPadding = calculateLeftPadding(method);


        final List<String> showActions = Arrays.asList("bug修复", "注释生成", "代码解释", "文案33333");

        final List<InlayPresentation> presentations = new SmartList<>();
        // 左边空格
        presentations.add(factory.textSpacePlaceholder(leftPadding, true));
        for (int i = 0; i < showActions.size(); i++) {
            String showAction = showActions.get(i);
            // 1.行为图标/文本
            //      1.1.展示内容
            final InlayPresentation base = switch (i) {
                case 0 ->
                        //          1.1.1 可折叠和展开效果
                        factory.collapsible(factory.text("")
                                , factory.text(showAction.substring(0, 3) + "...")
                                , () -> factory.text(showAction)
                                , factory.icon(AllIcons.Actions.FindAndShowNextMatchesSmall)
                                , true);
                case 1 ->
                        //          1.1.2 可展开效果
                        factory.folding(factory.text(showAction.substring(0, 3) + "..."), () -> factory.text(showAction));
                case 2 ->
                        //          1.1.3 文本拼接
                        factory.join(Arrays.asList(factory.text("a"), factory.text(showAction), factory.text("b")), () -> factory.text("|"));
                default ->
                        //          1.1.4 鼠标悬停效果
                        factory.changeOnHover(factory.text(showAction), () -> factory.text(showAction), mouseEvent -> true);
            };
            //          添加空白的边距
            final InsetPresentation inset = factory.inset(base, 0, 0, 0, 0);


            //      1.2.点击效果
            InlayPresentationFactory.ClickListener clickListener = (mouseEvent, point) -> {
                TextRange range = method.getTextRange();
                editor.getSelectionModel().setSelection(range.getStartOffset(), range.getEndOffset());
                System.out.println("click showAction:" + showAction + "\tmethod:" + method.getName() + "\tline:" + editor.getDocument().getLineNumber(method.getTextOffset()));
            };
            final InlayPresentation clickTextPresent = factory.referenceOnHover(inset, clickListener);
            presentations.add(clickTextPresent);

            // 2.分隔符(最后一个不添加)
            if (i != showActions.size() - 1) {
                presentations.add(factory.text(" | "));
            }
        }
        return new SequencePresentation(presentations);
    }

    /**
     * 生成下拉的提示,多个action会弹出选择框
     */
    private InlayPresentation generateInlayPresentationDropdown(Editor editor, PsiMethod method) {
        final PresentationFactory factory = new PresentationFactory(editor);
        // 计算需要的左边距
        int leftPadding = calculateLeftPadding(method);
        final List<InlayPresentation> presentations = new SmartList<>();
        presentations.add(factory.textSpacePlaceholder(leftPadding, true));
        presentations.add(factory.icon(MyIcons.logo));
        presentations.add(factory.smallScaledIcon(AllIcons.Actions.FindAndShowNextMatchesSmall));
        presentations.add(factory.textSpacePlaceholder(1, true));
        final SequencePresentation shiftedPresentation = new SequencePresentation(presentations);
        InlayPresentationFactory.ClickListener clickListener = (mouseEvent, point) -> new MyInlayHintsClickEventDropdownPopup().click(mouseEvent, point, editor, method);
        return factory.referenceOnHover(shiftedPresentation, clickListener);
    }


    /**
     * 计算左边距
     */
    private int calculateLeftPadding(PsiMethod method) {
        // 先拿到方法签名那行的真实文本
        final Document document = editor.getDocument();
        final int textOffset = method.getTextOffset();
        final int lineNumber = document.getLineNumber(textOffset);
        final TextRange textRange = new TextRange(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber));
        final String methodText = document.getText(textRange);

        // 根据空格还有tab计算个数
        int leftPadding = 0;
        int tabWidth = editor.getSettings().getTabSize(editor.getProject());
        for (char c : methodText.toCharArray()) {
            if (c == '\t') {
                leftPadding += tabWidth;
            } else if (Character.isWhitespace(c)) {
                leftPadding++;
            } else {
                break;
            }
        }
        return leftPadding;
    }


}