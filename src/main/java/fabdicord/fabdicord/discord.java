package fabdicord.fabdicord;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.Suggestions;
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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static fabdicord.fabdicord.Fabdicord.*;

public class discord extends ListenerAdapter {

    public static JDA jda;

    public static TextChannel PMChannel;

    public static Optional<TextChannel> NoticeChannel = Optional.empty();

    public static ThreadChannel LogChannel;

    public static String CommandChannel = "";

    public static ArrayList<String> Mpool = new ArrayList<>();

    public static ArrayList<MessageEmbed> Epool = new ArrayList<>();

    public static Role CommandRole;

    public static Thread log;

    public static void init() {
        jda = JDABuilder.createDefault(config.get("BotToken"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new discord())
                .build();

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        PMChannel = Optional.ofNullable(jda.getTextChannelById(config.get("PMChannelID"))).orElseThrow();

        if (NoticeChannel.isEmpty())
            NoticeChannel = Optional.ofNullable(jda.getTextChannelById(config.get("NoticeChannelID")));

        if (LogChannel == null) {
            Optional.ofNullable(jda.getThreadChannelById(config.get("LogChannelID"))).ifPresent(logChannel -> {
                LogChannel = logChannel;
                (log = new Thread(new log(true))).start();
            });
        }

        if (CommandChannel.isEmpty()) CommandChannel = config.get("CommandChannelID");

        if (CommandRole == null) CommandRole = jda.getRoleById(config.get("CommandRoleID"));

        sendMessage("✅ [" + ServerName + "] が起動しました", false);

        PMChannel.sendMessage("VELOCITY&OK&" + ServerName).queue();
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
        if (!event.getChannel().getId().equals(PMChannel.getId())) return;
        String[] data = event.getMessage().getContentDisplay().split("&");
        if (data[0].equals(ServerName) || data[0].equals("ALL")) {
            if (data[1].equals("OK")) {
                (NoticeChannel = Optional.ofNullable(jda.getTextChannelById(data[2]))).orElseThrow();
                config.put("NoticeChannelID", data[2]);
                if (!"".equals(data[3])) Optional.ofNullable(jda.getForumChannelById(data[3])).ifPresent(forum -> {
                    forum.getThreadChannels().stream().filter(thread -> ServerName.equals(thread.getName())).findFirst().ifPresentOrElse(
                            log -> LogChannel = log,

                            () -> LogChannel = Objects.requireNonNull(jda.getForumChannelById(data[3]))
                                    .createForumPost(ServerName, MessageCreateData.fromContent(ServerName + "'s log")).complete().getThreadChannel()
                    );
                    if (log == null) (log = new Thread(new log(true))).start();
                    else if (!log.isAlive()) (log = new Thread(new log(false))).start();
                    config.put("LogChannelID", LogChannel.getId());
                });
                config.put("CommandChannelID", (CommandChannel = data[4]));
                CommandRole = Optional.ofNullable(jda.getRoleById(data[5])).orElseThrow();
                config.put("CommandRoleID", data[5]);

                if (!event.getMessage().getAttachments().isEmpty()) {
                    event.getMessage().getAttachments().get(0).downloadToFile(ignorecommandjson.toFile());
                    event.getMessage().getAttachments().get(1).downloadToFile(disadmincommandjson.toFile());
                    event.getMessage().getAttachments().get(2).downloadToFile(mineadmincommandjson.toFile());

                    try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(ignorecommandjson)), StandardCharsets.UTF_8))) {
                        ignorecommand = gson.fromJson(reader, typeToken);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(disadmincommandjson)), StandardCharsets.UTF_8))) {
                        disadmincommand = gson.fromJson(reader, typeToken);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(mineadmincommandjson)), StandardCharsets.UTF_8))) {
                        mineadmincommand = gson.fromJson(reader, typeToken);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                Mpool.forEach(msg -> sendMessage(msg, false));

                Epool.forEach(msg -> sendMessage(msg, false));
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!CommandChannel.equals(event.getChannelId()) || (!Objects.requireNonNull(event.getMember()).getRoles().contains(CommandRole) &&
                disadmincommand.stream().anyMatch(event.getCommandString().substring(1)::startsWith))) return;

        switch (event.getName()) {
            case "ch" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "set" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "log" -> {
                                ForumChannel forumChannel = event.getOptions().get(1).getAsChannel().asForumChannel();
                                forumChannel.getThreadChannels().stream().filter(thread -> ServerName.equals(thread.getName())).findFirst().ifPresentOrElse(
                                        log -> LogChannel = log,

                                        () -> LogChannel = forumChannel.createForumPost(ServerName, MessageCreateData.fromContent(ServerName + "'s log"))
                                                .complete().getThreadChannel()
                                );
                                if (log == null) (log = new Thread(new log(true))).start();
                                else if (!log.isAlive()) (log = new Thread(new log(false))).start();
                                config.put("LogChannelID", LogChannel.getId());
                            }

                            case "pm" -> {
                                PMChannel = event.getOptions().get(1).getAsChannel().asTextChannel();
                                config.put("PMChannelID", PMChannel.getId());
                            }

                            case "notice" -> {
                                NoticeChannel = Optional.of(event.getOptions().get(1).getAsChannel().asTextChannel());
                                config.put("LogChannelID", NoticeChannel.get().getId());
                            }

                            case "command" -> {
                                CommandChannel = event.getOptions().get(1).getAsChannel().asTextChannel().getId();
                                config.put("CommandChannelID", CommandChannel);
                            }
                        }
                    }

                    case "del_log" -> {
                        log.interrupt();
                        config.put("LogChannelID", "000000");
                    }
                }
            }

            case "commandrole" -> {
                if ("set".equals(event.getSubcommandName())) CommandRole = event.getOptions().get(0).getAsRole();
            }

            case "ignorecommand" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "add" -> ignorecommand.add(event.getOptions().get(0).getAsString());
                    case "del" -> ignorecommand.remove(event.getOptions().get(0).getAsString());
                }
            }

            case "server" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "info" -> {
                        StringBuilder builder = new StringBuilder("```\n");

                        List<ServerPlayerEntity> onlinePlayers = server.getPlayerManager().getPlayerList();
                        builder.append("オンラインのプレイヤー ( ").append(onlinePlayers.size()).append(" / ").append(server.getMaxPlayerCount()).append(" ):\n");

                        if (onlinePlayers.isEmpty()) builder.append("<<オンラインのプレイヤーはいません>>\n");
                        else
                            onlinePlayers.forEach(player -> builder.append("[").append(player.pingMilliseconds).append("ms] ")
                                    .append(player instanceof EntityPlayerMPFake ? "(bot)" : "").append(Objects.requireNonNull(player.getDisplayName()).getString()).append("\n"));

                        double mspt = server.getTickTime();
                        builder.append("\nTPS:\n").append(Math.min(1000.0 / mspt, 20.0)).append("\n\nMSPT:\n").append(mspt).append("\n\n使用メモリ:\n")
                                .append((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L).append(" MB / ")
                                .append(Runtime.getRuntime().totalMemory() / 1024L / 1024L).append(" MB\n```");

                        event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                                .setTitle(ServerName + " info")
                                .setDescription(builder.toString())
                                .setColor(Color.green)
                                .build()
                        ).queue();
                    }

                    case "command" -> {
                        if (!ServerName.equals(event.getOptions().get(0).getAsString())) return;

                        String command = Objects.requireNonNull(event.getOptions()).get(1).getAsString();
                        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(CommandRole) &&
                                mineadmincommand.stream().anyMatch(command::startsWith)) {
                            event.replyEmbeds(new EmbedBuilder()
                                    .setColor(Color.red)
                                    .setTitle("このコマンドを実行するのに必要な権限がありません")
                                    .build()
                            ).setEphemeral(true).queue();
                            return;
                        }

                        server.getCommandManager().execute(server.getCommandSource().withOutput(new DiscordCommandSource(event)), command);
                    }
                }
            }

            case "admincommand" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "add" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "discord" -> disadmincommand.add(event.getOptions().get(1).getAsString());
                            case "マイクラ" -> mineadmincommand.add(event.getOptions().get(1).getAsString());
                        }
                    }

                    case "del" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "discord" -> disadmincommand.remove(event.getOptions().get(1).getAsString());
                            case "マイクラ" -> mineadmincommand.remove(event.getOptions().get(1).getAsString());
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
                !ServerName.equals(event.getOptions().get(0).getAsString())) return;

        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();

        String input = event.getFocusedOption().getValue();

        List<String> temp = new ArrayList<>();

        ParseResults<ServerCommandSource> results = dispatcher.parse(input, server.getCommandSource());
        Suggestions suggestions;
        try {
            suggestions = dispatcher.getCompletionSuggestions(results).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        int size = results.getContext().getNodes().size();
        if (size > 0) {
            dispatcher.getSmartUsage(((ParsedCommandNode) results.getContext().getNodes().get(size - 1)).getNode(), server.getCommandSource()).values()
                    .forEach(s -> temp.add((((String) s).length() > 100) ? ((String) s).substring(0, 99) : (String) s));
        }

        suggestions.getList().forEach(suggestion -> temp.add(suggestion.apply(input)));

        List<Command.Choice> options = temp.stream().map(command -> new Command.Choice(command, command)).toList();

        if (options.size() > 25) {
            options = options.subList(0, 23);
            options.add(new Command.Choice("...", "..."));
        }

        event.replyChoices(options).queue();
    }
}
