package com.github.nekozuki0509.fabdicord.common.minecraft;

public interface MinecraftDeathEvent {
    MinecraftPlayer getPlayer();

    String getDeathMessage();

    String getDimensionName();

    int getX();

    int getY();

    int getZ();

    default String getPlace() {
        return "%s:(%d, %d, %d)".formatted(getDimensionName(), getX(), getY(), getZ());
    }
}