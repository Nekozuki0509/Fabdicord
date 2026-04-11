package com.github.nekozuki0509.common.pmConnection;

import com.github.nekozuki0509.common.Common;
import com.github.nekozuki0509.common.Discord;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Optional;

public class DiscordPluginMessageManager extends PluginMessageManager {

    @Getter
    @Setter
    private TextChannel PMChannel;

    public DiscordPluginMessageManager() {
        super();

        this.PMChannel = Optional.ofNullable(Discord.getJda().getTextChannelById(Common.getConfig().get("PMChannelID"))).orElseThrow();
        sendMessage("OK&%s".formatted(Common.getServerName()));
    }

    @Override
    public void sendMessage(String msg) {
        PMChannel.sendMessage("VELOCITY&%s".formatted(msg)).complete();
    }
}
