package com.github.nekozuki0509.common.minecraft;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public interface MinecraftServerApi {
    String getServerIp();

    int getServerPort();

    List<MinecraftPlayer> getOnlinePlayers();

    int getMaxPlayerCount();

    double getMspt();

    void executeCommand(String command, SlashCommandInteractionEvent event);

    List<String> getCommandSuggestions(String input);
}