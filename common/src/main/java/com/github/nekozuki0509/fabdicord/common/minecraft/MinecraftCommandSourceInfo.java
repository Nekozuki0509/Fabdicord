package com.github.nekozuki0509.fabdicord.common.minecraft;

public interface MinecraftCommandSourceInfo {
    boolean isPlayer();

    String getExecutorName();
}