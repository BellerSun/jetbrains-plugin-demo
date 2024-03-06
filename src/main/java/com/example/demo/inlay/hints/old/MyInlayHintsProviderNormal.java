package com.example.demo.inlay.hints.old;

import com.intellij.codeInsight.hints.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
@SuppressWarnings("UnstableApiUsage")
public class MyInlayHintsProviderNormal implements InlayHintsProvider<MyInlayHintsProviderNormal.Settings> {


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
        return new SettingsKey<>("MyInlayHintsProviderNormal.getKey");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "MyInlayHintsProviderNormal.getName";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "MyInlayHintsProviderNormal.getPreviewText";
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull MyInlayHintsProviderNormal.Settings settings) {
        return new Settings();
    }

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull MyInlayHintsProviderNormal.Settings settings, @NotNull InlayHintsSink inlayHintsSink) {
        return new MyInlayHintsCollectorNormal(editor);
    }

    public static class Settings implements ImmediateConfigurable {
        @NotNull
        @Override
        public JComponent createComponent(@NotNull ChangeListener changeListener) {
            return new JLabel("MyInlayHintsProviderNormal.Settings createComponent");
        }
        // You can add settings here if needed
    }
}