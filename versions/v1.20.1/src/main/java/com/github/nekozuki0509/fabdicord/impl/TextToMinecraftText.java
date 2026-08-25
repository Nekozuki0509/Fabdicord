package com.github.nekozuki0509.fabdicord.impl;

import com.github.nekozuki0509.fabdicord.common.minecraft.MinecraftText;
import net.minecraft.text.Text;

public class TextToMinecraftText implements MinecraftText {
    private final Text text;

    public TextToMinecraftText(Text text) {
        this.text = text;
    }

    @Override
    public String getString() {
        return text.getString();
    }
}
