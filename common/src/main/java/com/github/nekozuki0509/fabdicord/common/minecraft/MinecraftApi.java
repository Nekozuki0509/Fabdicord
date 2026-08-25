package com.github.nekozuki0509.fabdicord.common.minecraft;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MinecraftApi {
    Path getConfigDir();

    Path getGameDir();

    void registerPosCommand(
            Consumer<MinecraftCommandSource> onPos,
            BiConsumer<MinecraftCommandSource, String> onNamedPos
    );

    void onPlayerJoin(Consumer<MinecraftPlayer> handler);

    void onPlayerDisconnect(Consumer<MinecraftPlayer> handler);

    void onServerStopped(Runnable handler);

    void onServerStarting(Consumer<MinecraftServerApi> handler);

    void onCommandExecuted(BiConsumer<MinecraftCommandSourceInfo, String> handler);

    void onPlayerAdvancement(Consumer<MinecraftAdvancementEvent> handler);

    void onPlayerDeath(Consumer<MinecraftDeathEvent> handler);
}