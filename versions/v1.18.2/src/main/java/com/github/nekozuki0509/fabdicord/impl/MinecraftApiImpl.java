package com.github.nekozuki0509.fabdicord.impl;

import com.github.nekozuki0509.common.Common;
import com.github.nekozuki0509.common.minecraft.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MinecraftApiImpl implements MinecraftApi {

    public static MinecraftApiImpl INSTANCE;

    private BiConsumer<MinecraftCommandSourceInfo, String> commandExecutedHandler;
    private Consumer<MinecraftAdvancementEvent> advancementHandler;
    private Consumer<MinecraftDeathEvent> deathHandler;

    public MinecraftApiImpl() {
        INSTANCE = this;
    }

    // ----------------------------------------------------------------
    // MinecraftApi impl
    // ----------------------------------------------------------------

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public void registerPosCommand(
            Consumer<MinecraftCommandSource> onPos,
            BiConsumer<MinecraftCommandSource, String> onNamedPos
    ) {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
                dispatcher.register(literal("pos")
                        .executes(ctx -> {
                            onPos.accept(toCommandSource(ctx.getSource()));
                            return 1;
                        })
                        .then(argument("name", string())
                                .executes(ctx -> {
                                    onNamedPos.accept(toCommandSource(ctx.getSource()), getString(ctx, "name"));
                                    return 1;
                                })
                        )
                )
        );
    }

    @Override
    public void onPlayerJoin(Consumer<MinecraftPlayer> handler) {
        ServerPlayConnectionEvents.JOIN.register((h, sender, s) ->
                handler.accept(new MinecraftPlayerImpl(h.player)));
    }

    @Override
    public void onPlayerDisconnect(Consumer<MinecraftPlayer> handler) {
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) ->
                handler.accept(new MinecraftPlayerImpl(h.player)));
    }

    @Override
    public void onServerStopped(Runnable handler) {
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> handler.run());
    }

    @Override
    public void onServerStarting(Consumer<MinecraftServerApi> handler) {
        ServerLifecycleEvents.SERVER_STARTING.register(s ->
                handler.accept(new MinecraftServerApiImpl(s)));
    }

    @Override
    public void onCommandExecuted(BiConsumer<MinecraftCommandSourceInfo, String> handler) {
        this.commandExecutedHandler = handler;
    }

    @Override
    public void onPlayerAdvancement(Consumer<MinecraftAdvancementEvent> handler) {
        this.advancementHandler = handler;
    }

    @Override
    public void onPlayerDeath(Consumer<MinecraftDeathEvent> handler) {
        this.deathHandler = handler;
    }

    // ----------------------------------------------------------------
    // Mixin から呼ばれる fire メソッド
    // ----------------------------------------------------------------

    public void fireCommandExecuted(ServerCommandSource source, String command) {
        if (commandExecutedHandler == null) return;
        commandExecutedHandler.accept(new MinecraftCommandSourceInfo() {
            @Override
            public boolean isPlayer() {
                return source.getEntity() instanceof ServerPlayerEntity;
            }

            @Override
            public String getExecutorName() {
                try {
                    return isPlayer()
                            ? source.getPlayer().getName().getString()
                            : source.getDisplayName().getString();
                } catch (Exception e) {
                    return source.getDisplayName().getString();
                }
            }
        }, command);
    }

    public void firePlayerAdvancement(
            ServerPlayerEntity player,
            String title, String description,
            String frameName,
            String velocityColor, String completionWord
    ) {
        if (advancementHandler == null) return;
        advancementHandler.accept(new MinecraftAdvancementEvent() {
            @Override
            public MinecraftPlayer getPlayer() {
                return new MinecraftPlayerImpl(player);
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public String getFrameName() {
                return frameName;
            }

            @Override
            public String getVelocityColor() {
                return velocityColor;
            }

            @Override
            public String getVelocityCompletionWord() {
                return completionWord;
            }
        });
    }

    public void firePlayerDeath(
            ServerPlayerEntity player,
            String deathMessage,
            String dimensionName, int x, int y, int z
    ) {
        if (deathHandler == null) return;
        deathHandler.accept(new MinecraftDeathEvent() {
            @Override
            public MinecraftPlayer getPlayer() {
                return new MinecraftPlayerImpl(player);
            }

            @Override
            public String getDeathMessage() {
                return deathMessage;
            }

            @Override
            public String getDimensionName() {
                return dimensionName;
            }

            @Override
            public int getX() {
                return x;
            }

            @Override
            public int getY() {
                return y;
            }

            @Override
            public int getZ() {
                return z;
            }
        });
    }

    // ----------------------------------------------------------------
    // 変換ヘルパー
    // ----------------------------------------------------------------

    private MinecraftCommandSource toCommandSource(ServerCommandSource source) {
        return new MinecraftCommandSource() {
            @Override
            public MinecraftPlayer getPlayer() {
                try {
                    return source.getPlayer() != null ? new MinecraftPlayerImpl(source.getPlayer()) : null;
                } catch (CommandSyntaxException e) {
                    Common.getLOGGER().error("Failed to get player from command source: {}", ExceptionUtils.getStackTrace(e));
                }
                return null;
            }

            @Override
            public int getX() {
                return (int) source.getPosition().x;
            }

            @Override
            public int getY() {
                return (int) source.getPosition().y;
            }

            @Override
            public int getZ() {
                return (int) source.getPosition().z;
            }

            @Override
            public String getDimensionName() {
                var key = source.getWorld().getRegistryKey();
                if (key == World.OVERWORLD) return "OVERWORLD";
                if (key == World.NETHER) return "NETHER";
                return "END";
            }
        };
    }
}