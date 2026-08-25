package com.github.nekozuki0509.fabdicord.impl;

import com.github.nekozuki0509.fabdicord.common.DiscordCommandSource;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;

import java.util.UUID;

public class DiscordCommandSourceImpl extends DiscordCommandSource implements CommandOutput {

    public DiscordCommandSourceImpl(SlashCommandInteractionEvent event) {
        super(event);
    }

    public void sendSystemMessage(Text message, UUID sender) {
        super.sendSystemMessage(new TextToMinecraftText(message), sender);
    }

    @Override
    public void sendMessage(Text message) {
        sendSystemMessage(message, null);
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return super.shouldReceiveFeedbackImpl();
    }

    @Override
    public boolean shouldTrackOutput() {
        return super.shouldTrackOutputImpl();
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return super.shouldBroadcastConsoleToOpsImpl();
    }
}
