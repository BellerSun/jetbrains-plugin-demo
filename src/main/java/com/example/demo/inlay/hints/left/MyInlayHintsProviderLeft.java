package com.example.demo.inlay.hints.left;

import com.intellij.codeInsight.hints.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MyInlayHintsProviderLeft implements InlayHintsProvider<MyInlayHintsProviderLeft.Settings> {


    @NotNull
    @Override
    public InlayGroup getGroup() {
        return InlayGroup.CODE_VISION_GROUP_NEW;
    }

    @NotNull
    @Override
    public Settings createSettings() {
        return new Settings();
    }

    @NotNull
    @Override
    public SettingsKey<Settings> getKey() {
        return new SettingsKey<>("MyInlayHintsProviderLeft.getKey");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "MyInlayHintsProviderLeft.getName";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "MyInlayHintsProviderLeft.getPreviewText";
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull MyInlayHintsProviderLeft.Settings settings) {
        return new Settings();
    }

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull MyInlayHintsProviderLeft.Settings settings, @NotNull InlayHintsSink inlayHintsSink) {
        return new MyInlayHintsCollectorLeft(editor, inlayHintsSink);
    }

    public static class Settings implements ImmediateConfigurable {
        @NotNull
        @Override
        public JComponent createComponent(@NotNull ChangeListener changeListener) {
            return new JLabel("MyInlayHintsProviderLeft.Settings createComponent");
        }
        // You can add settings here if needed
    }
}