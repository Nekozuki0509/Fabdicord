package com.github.nekozuki0509.common.minecraft;

public interface MinecraftCommandSourceInfo {
    boolean isPlayer();

    String getExecutorName();
}