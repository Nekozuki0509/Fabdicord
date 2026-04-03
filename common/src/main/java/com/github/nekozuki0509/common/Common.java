package com.github.nekozuki0509.common;

import com.github.nekozuki0509.common.minecraft.MinecraftApi;
import com.github.nekozuki0509.common.minecraft.MinecraftServerApi;
import com.github.nekozuki0509.common.pmConnection.DiscordPluginMessageManager;
import com.github.nekozuki0509.common.pmConnection.PluginMessageManager;
import com.github.nekozuki0509.common.pmConnection.WebSocketPluginMessageManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.nekozuki0509.common.Discord.sendMessage;

public class Common {

    @Getter
    private static MinecraftApi api;

    @Getter
    private volatile static MinecraftServerApi server;

    @Getter
    private static final Logger LOGGER = LoggerFactory.getLogger("fabdicord");

    @Getter
    private static PluginMessageManager PMManager;

    private static Path configjson;

    @Getter
    private static Path ignorecommandjson;

    @Getter
    private static Path disadmincommandjson;

    @Getter
    private static Path mineadmincommandjson;

    @Getter
    private static Map<String, String> config;

    @Getter
    @Setter
    private static List<String> ignorecommand;

    @Getter
    @Setter
    private static ArrayList<String> disadmincommand;

    @Getter
    @Setter
    private static ArrayList<String> mineadmincommand;

    @Getter
    private static String ServerName;

    @Getter
    private static int WebSocketPortIncrement;

    @Getter
    private static final Gson gson = new Gson();

    @Getter
    private static final Type typeToken = new TypeToken<ArrayList<String>>() {
    }.getType();

    public static void init(MinecraftApi api) {
        Common.api = api;
        Path ConfigDir = api.getConfigDir().resolve("Fabdicord");

        if (Files.notExists(ConfigDir)) {
            try {
                Files.createDirectory(ConfigDir);
            } catch (IOException e) {
                LOGGER.error("CONFIG FOLDER CREATE FAILED: {}", ExceptionUtils.getStackTrace(e));
            }
        }

        configjson = ConfigDir.resolve("config.json");
        ignorecommandjson = ConfigDir.resolve("ignorecommand.json");
        disadmincommandjson = ConfigDir.resolve("disadmincommand.json");
        mineadmincommandjson = ConfigDir.resolve("mineadmincommand.json");

        if (Files.notExists(configjson)) {
            try {
                Files.copy(Objects.requireNonNull(Common.class.getResourceAsStream("/config.json")), configjson);
                LOGGER.info("fabdicordのconfigを設定してください");
            } catch (IOException e) {
                LOGGER.error("CONFIG FILE CREATE FAILED: {}", ExceptionUtils.getStackTrace(e));
            }
        }
        if (Files.notExists(ignorecommandjson)) {
            try {
                Files.copy(Objects.requireNonNull(Common.class.getResourceAsStream("/ignorecommand.json")), ignorecommandjson);
            } catch (IOException e) {
                LOGGER.error("IGNORE COMMAND FILE CREATE FAILED: {}", ExceptionUtils.getStackTrace(e));
            }
        }
        if (Files.notExists(disadmincommandjson)) {
            try {
                Files.copy(Objects.requireNonNull(Common.class.getResourceAsStream("/disadmincommand.json")), disadmincommandjson);
            } catch (IOException e) {
                LOGGER.error("DISADMIN COMMAND FILE CREATE FAILED: {}", ExceptionUtils.getStackTrace(e));
            }
        }
        if (Files.notExists(mineadmincommandjson)) {
            try {
                Files.copy(Objects.requireNonNull(Common.class.getResourceAsStream("/mineadmincommand.json")), mineadmincommandjson);
            } catch (IOException e) {
                LOGGER.error("MINEADMIN COMMAND FILE CREATE FAILED: {}", ExceptionUtils.getStackTrace(e));
            }
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(configjson)), StandardCharsets.UTF_8))) {
            config = gson.fromJson(reader, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (IOException e) {
            LOGGER.error("CONFIG FILE LOAD FAILED: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(ignorecommandjson)), StandardCharsets.UTF_8))) {
            ignorecommand = gson.fromJson(reader, typeToken);
        } catch (IOException e) {
            LOGGER.error("IGNORE COMMAND FILE LOAD FAILED: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(disadmincommandjson)), StandardCharsets.UTF_8))) {
            disadmincommand = gson.fromJson(reader, typeToken);
        } catch (IOException e) {
            LOGGER.error("DISADMIN COMMAND FILE LOAD FAILED: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(mineadmincommandjson)), StandardCharsets.UTF_8))) {
            mineadmincommand = gson.fromJson(reader, typeToken);
        } catch (IOException e) {
            LOGGER.error("MINEADMIN COMMAND FILE LOAD FAILED: {}", ExceptionUtils.getStackTrace(e));
        }

        ServerName = config.get("ServerName");

        WebSocketPortIncrement = Integer.parseInt(config.get("WebSocketPortIncrement"));

        PMManager = config.get("PMType").equals("1") ? new DiscordPluginMessageManager() : new WebSocketPluginMessageManager();

        api.registerPosCommand(
                source -> {
                    if (source.getPlayer() == null) return;
                    String PN = source.getPlayer().getName();
                    getPMManager().sendMessage("POS&%s&%s&%s&(%d, %d, %d)".formatted(
                            ServerName, PN, source.getDimensionName(),
                            source.getX(), source.getY(), source.getZ()
                    ));
                },
                (source, name) -> {
                    if (source.getPlayer() == null) return;
                    String PN = source.getPlayer().getName();
                    getPMManager().sendMessage("NPOS&%s&%s&%s&(%d, %d, %d)&%s".formatted(
                            ServerName, PN, source.getDimensionName(),
                            source.getX(), source.getY(), source.getZ(), name
                    ));
                }
        );

        api.onPlayerJoin(player -> {
            if (!player.isFakePlayer()) return;
            String PN = player.getName();
            sendMessage(new EmbedBuilder()
                    .setTitle("[%s] に入室しました".formatted(ServerName))
                    .setColor(Color.blue)
                    .setAuthor("(BOT) %s".formatted(PN), null,
                            "https://mc-heads.net/avatar/%s.png".formatted(PN))
                    .build(), false);
            getPMManager().sendMessage(
                    "READ&<yellow><aqua>[<blue>(BOT)</blue> %s]</aqua> が <dark_green>[%s]</dark_green> に入室しました&(bot) %sが%sに入室しました"
                            .formatted(PN, ServerName, PN, ServerName));
        });

        api.onPlayerDisconnect(player -> {
            if (!player.isFakePlayer()) return;
            String PN = player.getName();
            sendMessage(new EmbedBuilder()
                    .setTitle("退出しました")
                    .setColor(Color.blue)
                    .setAuthor("(BOT) %s".formatted(PN), null,
                            "https://mc-heads.net/avatar/%s.png".formatted(PN))
                    .build(), false);
            getPMManager().sendMessage(
                    "READ&<aqua>[<blue>(BOT)</blue> %s]</aqua> <yellow>が退出しました&(bot) %sがマイクラサーバーから退出しました"
                            .formatted(PN, PN));
        });

        api.onServerStopped(() -> {
            getPMManager().sendMessage("FIN&%s".formatted(ServerName));
            if (PMManager instanceof WebSocketPluginMessageManager manager) manager.stop();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(String.valueOf(configjson)), StandardCharsets.UTF_8))) {
                gson.toJson(config, writer);
            } catch (IOException e) {
                LOGGER.error("CONFIG FILE SAVE FAILED: {}", ExceptionUtils.getStackTrace(e));
            }
            sendMessage("\uD83D\uDED1 [%s] が停止しました".formatted(ServerName), true);
            Discord.getJda().shutdown();
        });

        api.onServerStarting(server -> Common.server = server);

        api.onCommandExecuted((source, command) -> {
            if (ignorecommand.stream().anyMatch(command::startsWith)) return;
            String executer = source.getExecutorName();
            Discord.sendMessage(new EmbedBuilder()
                    .setTitle("[%s]で[%s]を実行しました".formatted(ServerName, command))
                    .setColor(Color.yellow)
                    .setAuthor(executer, null, "https://mc-heads.net/avatar/%s.png".formatted(executer))
                    .build(), false);
        });

        api.onPlayerAdvancement(e -> {
            String PN = e.getPlayer().getName();
            String PNpre = e.getPlayer().isFakePlayer() ? "(BOT) " : "";
            String title = e.getTitle();
            String description = e.getDescription();
            String frame = e.getFrameName();
            String color = e.getVelocityColor();
            String completion = e.getVelocityCompletionWord();

            Discord.sendMessage(new EmbedBuilder()
                    .setTitle("[%s] で%s [%s] %sしました".formatted(ServerName, frame, title, completion))
                    .setDescription(description)
                    .setColor(Color.green)
                    .setAuthor("%s%s".formatted(PNpre, PN), null, "https://mc-heads.net/avatar/%s.png".formatted(PN))
                    .build(), false);

            getPMManager().sendMessage(
                    "VELOCITY&SEND&<yellow><dark_green>[%s]</dark_green> で <aqua>[<blue>%s</blue>%s]</aqua> が%s <hover:show_text:'<%s>%s\n%s</%s>'><%s>[%s]</%s></hover> を%sしました"
                            .formatted(ServerName, PNpre, PN, frame, color, title, description, color, color, title, color, completion)
            );
        });

        api.onPlayerDeath(e -> {
            String PN = e.getPlayer().getName();
            String PNpre = e.getPlayer().isFakePlayer() ? "(BOT) " : "";
            String place = e.getPlace();
            String message = e.getDeathMessage();

            Discord.sendMessage(new EmbedBuilder()
                    .setTitle("[%s] の [%s] で死亡しました".formatted(ServerName, place))
                    .setDescription(message)
                    .setColor(Color.red)
                    .setAuthor("%s%s".formatted(PNpre, PN), null, "https://mc-heads.net/avatar/%s.png".formatted(PN))
                    .build(), false);

            getPMManager().sendMessage(
                    "VELOCITY&READ&<hover:show_text:'%s'><red><dark_green>[%s]</dark_green><yellow>%s</yellow> で <aqua>[<blue>%s</blue>%s]</aqua> が死亡しました&%s%sで%s%sが死亡しました"
                            .formatted(message, ServerName, place, PNpre, PN, ServerName, place, PNpre, PN)
            );
        });

        LOGGER.info("fabdicord loaded");

        Discord.init();
    }
}