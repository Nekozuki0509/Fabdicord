package com.github.nekozuki0509.fabdicord.common;

import com.github.nekozuki0509.fabdicord.common.minecraft.MinecraftPlayer;
import com.github.nekozuki0509.fabdicord.common.pmConnection.DiscordPluginMessageManager;
import com.github.nekozuki0509.fabdicord.common.pmConnection.PluginMessageManager;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

import static com.github.nekozuki0509.fabdicord.common.Common.*;

public class Discord extends ListenerAdapter {

    @Getter
    private static JDA jda;

    @Getter
    @Setter
    private static Optional<TextChannel> NoticeChannel = Optional.empty();

    @Getter
    @Setter
    private static ThreadChannel LogChannel;

    @Getter
    @Setter
    private static String CommandChannel = "";

    @Getter
    private static final Queue<String> Mpool = new ArrayDeque<>();

    @Getter
    private static final Queue<MessageEmbed> Epool = new ArrayDeque<>();

    @Setter
    private static Role CommandRole;

    @Getter
    @Setter
    private static Thread log;

    public static void init() {
        jda = JDABuilder.createDefault(getConfig().get("BotToken"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new Discord())
                .build();

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            Common.getLOGGER().error("Failed to initialize Discord bot: {}", ExceptionUtils.getStackTrace(e));
        }

        if (NoticeChannel.isEmpty())
            NoticeChannel = Optional.ofNullable(jda.getTextChannelById(getConfig().get("NoticeChannelID")));

        if (LogChannel == null) {
            Optional.ofNullable(jda.getThreadChannelById(getConfig().get("LogChannelID"))).ifPresent(logChannel -> {
                LogChannel = logChannel;
                (log = new Thread(new Log(true))).start();
            });
        }

        if (CommandChannel.isEmpty()) CommandChannel = getConfig().get("CommandChannelID");

        if (CommandRole == null) CommandRole = jda.getRoleById(getConfig().get("CommandRoleID"));

        sendMessage("✅ [%s] が起動しました".formatted(getServerName()), false);
    }

    public static void sendMessage(String msg, boolean complete) {
        NoticeChannel.ifPresentOrElse(
                notice -> {
                    if (complete) notice.sendMessage(msg).complete();
                    else notice.sendMessage(msg).queue();
                },

                () -> Mpool.add(msg)
        );
    }

    public static void sendMessage(MessageEmbed msg, boolean complete) {
        NoticeChannel.ifPresentOrElse(
                notice -> {
                    if (complete) notice.sendMessageEmbeds(msg).complete();
                    else notice.sendMessageEmbeds(msg).queue();
                },

                () -> Epool.add(msg)
        );
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (getPMManager() instanceof DiscordPluginMessageManager manager && !event.getChannel().getId().equals(manager.getPMChannel().getId()))
            return;

        String[] data = event.getMessage().getContentDisplay().split("&");
        if (data[0].equals(Common.getServerName()) || data[0].equals("ALL")) {
            PluginMessageManager.receive(data);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!CommandChannel.equals(event.getChannelId()) || (!Objects.requireNonNull(event.getMember()).getRoles().contains(CommandRole) &&
                getDisadmincommand().stream().anyMatch(event.getCommandString().substring(1)::startsWith))) return;

        switch (event.getName()) {
            case "ch" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "set" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "log" -> {
                                ForumChannel forumChannel = event.getOptions().get(1).getAsChannel().asForumChannel();
                                forumChannel.getThreadChannels().stream().filter(thread -> getServerName().equals(thread.getName())).findFirst().ifPresentOrElse(
                                        log -> LogChannel = log,

                                        () -> LogChannel = forumChannel.createForumPost(getServerName(), MessageCreateData.fromContent("%s's log".formatted(getServerName())))
                                                .complete().getThreadChannel()
                                );
                                if (log == null) (log = new Thread(new Log(true))).start();
                                else if (!log.isAlive()) (log = new Thread(new Log(false))).start();
                                getConfig().put("LogChannelID", LogChannel.getId());
                            }

                            case "pm" -> {
                                if (Common.getPMManager() instanceof DiscordPluginMessageManager manager) {
                                    manager.setPMChannel(event.getOptions().get(1).getAsChannel().asTextChannel());
                                    getConfig().put("PMChannelID", manager.getPMChannel().getId());
                                }
                            }

                            case "notice" -> {
                                NoticeChannel = Optional.of(event.getOptions().get(1).getAsChannel().asTextChannel());
                                getConfig().put("LogChannelID", NoticeChannel.get().getId());
                            }

                            case "command" -> {
                                CommandChannel = event.getOptions().get(1).getAsChannel().asTextChannel().getId();
                                getConfig().put("CommandChannelID", CommandChannel);
                            }
                        }
                    }

                    case "del_log" -> {
                        log.interrupt();
                        getConfig().put("LogChannelID", "000000");
                    }
                }
            }

            case "commandrole" -> {
                if ("set".equals(event.getSubcommandName())) CommandRole = event.getOptions().get(0).getAsRole();
            }

            case "ignorecommand" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "add" -> getIgnorecommand().add(event.getOptions().get(0).getAsString());
                    case "del" -> getIgnorecommand().remove(event.getOptions().get(0).getAsString());
                }
            }

            case "server" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "info" -> {
                        StringBuilder builder = new StringBuilder("```\n");

                        List<MinecraftPlayer> onlinePlayers = getServer().getOnlinePlayers();
                        builder.append("オンラインのプレイヤー ( ").append(onlinePlayers.size()).append(" / ").append(getServer().getMaxPlayerCount()).append(" ):\n");

                        if (onlinePlayers.isEmpty()) builder.append("<<オンラインのプレイヤーはいません>>\n");
                        else
                            onlinePlayers.forEach(player -> builder.append("[").append(player.getPing()).append("ms] ")
                                    .append(player.isFakePlayer() ? "(bot)" : "").append(Objects.requireNonNull(player.getDisplayName())).append("\n"));

                        double mspt = getServer().getMspt();
                        builder.append("\nTPS:\n").append(Math.min(1000.0 / mspt, 20.0)).append("\n\nMSPT:\n").append(mspt).append("\n\n使用メモリ:\n")
                                .append((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L).append(" MB / ")
                                .append(Runtime.getRuntime().totalMemory() / 1024L / 1024L).append(" MB\n```");

                        event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                                .setTitle("%s info".formatted(getServerName()))
                                .setDescription(builder.toString())
                                .setColor(Color.green)
                                .build()
                        ).queue();
                    }

                    case "command" -> {
                        if (!getServerName().equals(event.getOptions().get(0).getAsString())) return;

                        String command = Objects.requireNonNull(event.getOptions()).get(1).getAsString();
                        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(CommandRole) &&
                                getMineadmincommand().stream().anyMatch(command::startsWith)) {
                            event.replyEmbeds(new EmbedBuilder()
                                    .setColor(Color.red)
                                    .setTitle("このコマンドを実行するのに必要な権限がありません")
                                    .build()
                            ).setEphemeral(true).queue();
                            return;
                        }

                        getServer().executeCommand(command, event);
                    }
                }
            }

            case "admincommand" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "add" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "discord" -> getDisadmincommand().add(event.getOptions().get(1).getAsString());
                            case "マイクラ" -> getMineadmincommand().add(event.getOptions().get(1).getAsString());
                        }
                    }

                    case "del" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "discord" -> getDisadmincommand().remove(event.getOptions().get(1).getAsString());
                            case "マイクラ" -> getMineadmincommand().remove(event.getOptions().get(1).getAsString());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!CommandChannel.equals(event.getChannelId()) || !"server".equals(event.getName()) ||
                !"command".equals(event.getSubcommandName()) || !"command".equals(event.getFocusedOption().getName()) ||
                !getServerName().equals(event.getOptions().get(0).getAsString())) return;

        String input = event.getFocusedOption().getValue();
        List<String> temp = getServer().getCommandSuggestions(input);

        List<Command.Choice> options = temp.stream()
                .map(c -> new Command.Choice(c, c))
                .collect(java.util.stream.Collectors.toList());

        if (options.size() > 25) {
            options = options.subList(0, 23);
            options.add(new Command.Choice("...", "..."));
        }

        event.replyChoices(options).queue();
    }
}