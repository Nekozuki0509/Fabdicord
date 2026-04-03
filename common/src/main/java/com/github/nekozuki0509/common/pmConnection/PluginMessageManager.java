package com.github.nekozuki0509.common.pmConnection;

import com.github.nekozuki0509.common.Common;
import com.github.nekozuki0509.common.Discord;
import com.github.nekozuki0509.common.Log;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Objects;
import java.util.Optional;

import static com.github.nekozuki0509.common.Common.getConfig;
import static com.github.nekozuki0509.common.Common.getServerName;

public abstract class PluginMessageManager {
    public static void receive(String msg) {
        String[] data = msg.split("&");
        if (data[0].equals(getServerName()) || data[0].equals("ALL")) {
            if (data[1].equals("OK")) {
                Discord.setNoticeChannel(Optional.ofNullable(Discord.getJda().getTextChannelById(data[2])));
                Discord.getNoticeChannel().orElseThrow();
                getConfig().put("NoticeChannelID", data[2]);
                if (!"".equals(data[3]))
                    Optional.ofNullable(Discord.getJda().getForumChannelById(data[3])).ifPresent(forum -> {
                        forum.getThreadChannels().stream().filter(thread -> getServerName().equals(thread.getName())).findFirst().ifPresentOrElse(
                                Discord::setLogChannel,

                                () -> Discord.setLogChannel(Objects.requireNonNull(Discord.getJda().getForumChannelById(data[3]))
                                        .createForumPost(getServerName(), MessageCreateData.fromContent("%s's log".formatted(getServerName()))).complete().getThreadChannel())
                        );
                        if (Discord.getLog() == null) {
                            Discord.setLog(new Thread(new Log(true)));
                            Discord.getLog().start();
                        } else if (!Discord.getLog().isAlive()) {
                            Discord.setLog(new Thread(new Log(false)));
                            Discord.getLog().start();
                        }
                        getConfig().put("LogChannelID", Discord.getLogChannel().getId());
                    });
                Discord.setCommandChannel(data[4]);
                getConfig().put("CommandChannelID", Discord.getCommandChannel());
                Discord.setCommandRole(Optional.ofNullable(Discord.getJda().getRoleById(data[5])).orElseThrow());
                getConfig().put("CommandRoleID", data[5]);

                Common.setIgnorecommand(Common.getGson().fromJson(data[6], Common.getTypeToken()));
                Common.setDisadmincommand(Common.getGson().fromJson(data[7], Common.getTypeToken()));
                Common.setMineadmincommand(Common.getGson().fromJson(data[8], Common.getTypeToken()));

                Discord.getMpool().removeIf(message -> {
                    Discord.sendMessage(message, false);
                    return true;
                });

                Discord.getEpool().removeIf(message -> {
                    Discord.sendMessage(message, false);
                    return true;
                });
            } else Common.getLOGGER().error("Unknown message received: %s".formatted(msg));
        }
    }

    public abstract void sendMessage(String msg);
}
