package com.github.nekozuki0509.common.minecraft;

public interface MinecraftPlayer {
    String getName();

    String getDisplayName();

    boolean isFakePlayer();

    int getPing();
}