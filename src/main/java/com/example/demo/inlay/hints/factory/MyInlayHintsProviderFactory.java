package com.example.demo.inlay.hints.factory;

import com.example.demo.inlay.hints.old.MyInlayHintsProviderNormal;
import com.intellij.codeInsight.hints.InlayHintsProvider;
import com.intellij.codeInsight.hints.InlayHintsProviderFactory;
import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class MyInlayHintsProviderFactory implements InlayHintsProviderFactory {


    @NotNull
    @Override
    public List<InlayHintsProvider<?>> getProvidersInfoForLanguage(@NotNull Language language) {
        return List.of(new MyInlayHintsProviderNormal());
    }
}
