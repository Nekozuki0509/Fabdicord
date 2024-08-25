package fabdicord.fabdicord;

import carpet.patches.EntityPlayerMPFake;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.netty.buffer.Unpooled;
import net.dv8tion.jda.api.EmbedBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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

import static fabdicord.fabdicord.discord.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Fabdicord implements ModInitializer {

    public static MinecraftServer server;

    public static final Logger LOGGER = LoggerFactory.getLogger("fabdicord");

    public static Path configjson;

    public static Path ignorecommandjson;

    public static Path disadmincommandjson;

    public static Path mineadmincommandjson;

    public static Map<String, String> config;

    public static List<String> ignorecommand;

    public static ArrayList<String> disadmincommand;

    public static ArrayList<String> mineadmincommand;

    public static String ServerName;

    public static Gson gson = new Gson();

    public static Type typeToken = new TypeToken<ArrayList<String>>() {
    }.getType();

    @Override
    public void onInitialize() {
        Path ConfigDir = FabricLoader.getInstance().getConfigDir().resolve("Fabdicord");

        if (Files.notExists(ConfigDir)) {
            try {
                Files.createDirectory(ConfigDir);
            } catch (IOException e) {
                throw new RuntimeException("Velodicordのconfigディレクトリを作れませんでした");
            }
        }

        configjson = ConfigDir.resolve("fabdicordconfig.json");
        ignorecommandjson = ConfigDir.resolve("ignorecommand.json");
        disadmincommandjson = ConfigDir.resolve("disadmincommand.json");
        mineadmincommandjson = ConfigDir.resolve("mineadmincommand.json");

        if (Files.notExists(configjson)) {
            try {
                Files.copy(Objects.requireNonNull(Fabdicord.class.getResourceAsStream("/fabdicordconfig.json")), configjson);
                LOGGER.info("fabdicordのconfigを設定してください");
                System.exit(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (Files.notExists(ignorecommandjson)) {
            try {
                Files.copy(Objects.requireNonNull(Fabdicord.class.getResourceAsStream("/ignorecommand.json")), ignorecommandjson);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (Files.notExists(disadmincommandjson)) {
            try {
                Files.copy(Objects.requireNonNull(Fabdicord.class.getResourceAsStream("/disadmincommand.json")), disadmincommandjson);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (Files.notExists(mineadmincommandjson)) {
            try {
                Files.copy(Objects.requireNonNull(Fabdicord.class.getResourceAsStream("/mineadmincommand.json")), mineadmincommandjson);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(configjson)), StandardCharsets.UTF_8))) {
            config = gson.fromJson(reader, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        ServerName = config.get("ServerName");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("pos")
                        .executes(ctx -> {
                            if (!ctx.getSource().isExecutedByPlayer()) return 1;

                            final ServerPlayerEntity self = ctx.getSource().getPlayer();
                            final Vec3d pos = ctx.getSource().getPosition();
                            PMChannel.sendMessage("POS&" + ServerName + "&" + Objects.requireNonNull(self).getName().getString() + "&"
                                    + (self.getWorld().getRegistryKey() == World.OVERWORLD ? "OVERWORLD" : self.getWorld().getRegistryKey() == World.NETHER ? "NETHER" : "END") + "&"
                                    + "(" + (int) pos.x + ", " + (int) pos.y + ", " + (int) pos.z + ")").queue();
                            return 1;
                        }).then(argument("name", StringArgumentType.string())
                                .executes(ctx -> {
                                    if (!ctx.getSource().isExecutedByPlayer()) return 1;

                                    final ServerPlayerEntity self = ctx.getSource().getPlayer();
                                    final Vec3d pos = ctx.getSource().getPosition();
                                    PMChannel.sendMessage("NPOS&" + ServerName + "&" + Objects.requireNonNull(self).getName().getString() + "&"
                                            + (self.getWorld().getRegistryKey() == World.OVERWORLD ? "OVERWORLD" : self.getWorld().getRegistryKey() == World.NETHER ? "NETHER" : "END") + "&"
                                            + "(" + (int) pos.x + ", " + (int) pos.y + ", " + (int) pos.z + ")&"
                                            + StringArgumentType.getString(ctx, "name")).queue();
                                    return 1;
                                })
                        )
                )
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            String PN = player.getName().getString();
            if (!(player instanceof EntityPlayerMPFake)) return;

            sendMessage(new EmbedBuilder()
                    .setTitle("[" + ServerName + "] に入室しました")
                    .setColor(Color.blue)
                    .setAuthor("(BOT) " + PN, null, "https://mc-heads.net/avatar/" + PN + ".png")
                    .build(), false);

            PMChannel.sendMessage("VELOCITY&READ&<yellow><aqua>[<blue>(BOT)</blue> " + PN + "]</aqua> が <dark_green>[" + ServerName + "]</dark_green> に入室しました&(bot) " + PN + "が" + ServerName + "に入室しました").queue();
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            String PN = player.getName().getString();
            if (!(player instanceof EntityPlayerMPFake)) return;

            sendMessage(new EmbedBuilder()
                    .setTitle("退出しました")
                    .setColor(Color.blue)
                    .setAuthor("(BOT) " + PN, null, "https://mc-heads.net/avatar/" + PN + ".png")
                    .build(), false);

            PMChannel.sendMessage("VELOCITY&READ&<aqua>[<blue>(BOT)</blue> " + PN + "]</aqua> <yellow>が退出しました&(bot) " + PN + "がマイクラサーバーから退出しました").queue();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            PMChannel.sendMessage("VELOCITY&FIN&" + ServerName).complete();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(configjson)), StandardCharsets.UTF_8))) {
                gson.toJson(config, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sendMessage("\uD83D\uDED1 [" + ServerName + "] が停止しました", true);
            jda.shutdown();
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> false);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> Fabdicord.server = server);

        LOGGER.info("fabdicord loaded");

        discord.init();
    }
}