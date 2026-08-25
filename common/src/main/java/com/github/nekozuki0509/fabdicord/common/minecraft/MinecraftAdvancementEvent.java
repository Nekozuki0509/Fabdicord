package com.github.nekozuki0509.fabdicord.common.minecraft;

public interface MinecraftAdvancementEvent {
    MinecraftPlayer getPlayer();

    String getTitle();

    String getDescription();

    String getFrameName();

    String getVelocityColor();

    String getVelocityCompletionWord();
}