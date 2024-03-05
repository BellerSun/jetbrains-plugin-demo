package com.example.demo.inlay.hints.old;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class MyInlayHintsClickEventDropdownPopup implements MyInlayHintsClickEvent{
    @Override
    public void click(@NotNull MouseEvent mouseEvent, @NotNull Point point, Editor editor, PsiMethod method) {

        System.out.println("click:" + "smallText" + "\tX:" + point.getX() + "\tY:" + point.getY() + "\tLY:" + point.getLocation().getX() + "\tY:" + point.getLocation().getY());

        List<String> popupActions = Arrays.asList("bug修复", "注释生成", "代码解释");
        JBPopup popup = JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<String>("", popupActions) {
            public @NotNull String getTextFor(String value) {
                return value;
            }

            public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
                TextRange range = method.getTextRange();
                editor.getSelectionModel().setSelection(range.getStartOffset(), range.getEndOffset());

                    /*
                    ActionManager actionManager = ActionManager.getInstance();
                    DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
                    AnActionEvent event = new AnActionEvent((InputEvent)null, dataContext, "MenuPopup", new Presentation(), actionManager, 0);
                    AnAction action = actionManager.getAction(selectedValue);
                    action.actionPerformed(event);*/
                System.out.println("selectedValue:" + selectedValue + "\tfinalChoice:" + finalChoice);
                return FINAL_CHOICE;
            }
        });
        popup.showInScreenCoordinates(editor.getComponent(), mouseEvent.getLocationOnScreen());
    }
}
