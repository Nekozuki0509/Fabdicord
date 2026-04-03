package com.github.nekozuki0509.common.minecraft;

public interface MinecraftCommandSource {
    MinecraftPlayer getPlayer();

    int getX();

    int getY();

    int getZ();

    String getDimensionName();
}