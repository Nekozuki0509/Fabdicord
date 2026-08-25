package com.github.nekozuki0509.fabdicord.impl;

import com.github.nekozuki0509.fabdicord.common.Common;
import com.github.nekozuki0509.fabdicord.common.minecraft.MinecraftPlayer;
import com.github.nekozuki0509.fabdicord.common.minecraft.MinecraftServerApi;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MinecraftServerApiImpl implements MinecraftServerApi {
    private final MinecraftServer server;

    public MinecraftServerApiImpl(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public String getServerIp() {
        return server.getServerIp();
    }

    public int getServerPort() {
        return server.getServerPort();
    }

    @Override
    public List<MinecraftPlayer> getOnlinePlayers() {
        return server.getPlayerManager().getPlayerList().stream()
                .map(MinecraftPlayerImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public int getMaxPlayerCount() {
        return server.getMaxPlayerCount();
    }

    @Override
    public double getMspt() {
        return server.getTickTime();
    }

    @Override
    public void executeCommand(String command, SlashCommandInteractionEvent event) {
        server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse(command,
                new ServerCommandSource(new DiscordCommandSourceImpl(event), Vec3d.ZERO, Vec2f.ZERO, server.getOverworld(), 4,
                        "Fabdicord", Text.literal("Fabdicord"), server, null)), command);
    }

    @Override
    public List<String> getCommandSuggestions(String input) {
        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
        ParseResults<ServerCommandSource> results = dispatcher.parse(input, server.getCommandSource());

        List<String> temp = new ArrayList<>();
        int size = results.getContext().getNodes().size();
        if (size > 0) {
            dispatcher.getSmartUsage(
                    results.getContext().getNodes().get(size - 1).getNode(),
                    server.getCommandSource()
            ).values().forEach(s -> temp.add(s.length() > 100 ? s.substring(0, 99) : s));
        }

        try {
            dispatcher.getCompletionSuggestions(results).get()
                    .getList().forEach(s -> temp.add(s.apply(input)));
        } catch (InterruptedException | ExecutionException e) {
            Common.getLOGGER().error("Failed to get command suggestions: {}", ExceptionUtils.getStackTrace(e));
        }

        return temp;
    }
}