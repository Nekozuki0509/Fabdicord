package com.github.nekozuki0509.common.minecraft;

public interface MinecraftCommandOutput {
    void sendFeedback(String message);

    void sendError(String message);
}