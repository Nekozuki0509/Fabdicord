package com.github.nekozuki0509.fabdicord.impl;

import carpet.patches.EntityPlayerMPFake;
import com.github.nekozuki0509.fabdicord.common.minecraft.MinecraftPlayer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class MinecraftPlayerImpl implements MinecraftPlayer {
    private final ServerPlayerEntity player;

    public MinecraftPlayerImpl(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName().getString();
    }

    @Override
    public String getDisplayName() {
        return Objects.requireNonNull(player.getDisplayName()).getString();
    }

    @Override
    public boolean isFakePlayer() {
        return player instanceof EntityPlayerMPFake;
    }

    @Override
    public int getPing() {
        return player.pingMilliseconds;
    }
}