package fabdicord.fabdicord;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.*;

public class Fabdicord implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabdicord");

	static Path configjson;

	public static Map<String, String> config;

	public static Gson gson = new Gson();

	@Override
	public void onInitialize() {
		configjson = FabricLoader.getInstance().getConfigDir().resolve("fabdicordconfig.json");
		if (Files.notExists(configjson)) {
			try {
				Files.copy(Objects.requireNonNull(Fabdicord.class.getResourceAsStream("/fabdicordconfig.json")), configjson);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(configjson)), StandardCharsets.UTF_8))) {
			config = gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		AdvancementCallback.EVENT.register((player, advancement) -> {
			//advancement type:server:player:title:description
			AdvancementDisplay display;
			if (!((display=advancement.getDisplay()) == null || display.isHidden()))
				ServerPlayNetworking.send(
						player,
						new Identifier("velocity", "fabdicord"),
						new PacketByteBuf(Unpooled.wrappedBuffer(("ADVANCEMENT:"+config.get("SERVER")+":"+player.getName().getString()+":"+display.getTitle().getString()+":"+display.getDescription().getString())
								.getBytes(StandardCharsets.UTF_8)))
				);
		});

		//pos type:server:player:dim:x:y:z
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
			dispatcher.register(literal("pos")
					.executes(ctx -> {
						final ServerPlayerEntity self = ctx.getSource().getPlayer();
						ServerPlayNetworking.send(
								Objects.requireNonNull(self),
								new Identifier("velocity", "fabdicord"),
								new PacketByteBuf(Unpooled.wrappedBuffer(("POS:" + config.get("SERVER") + ":" + Objects.requireNonNull(self).getName().getString() + ":"
										+ (self.getWorld().getRegistryKey()==World.OVERWORLD?"OVERWORLD":self.getWorld().getRegistryKey()==World.NETHER?"NETHER":"END") + ":"
										+ "(" + ((int) self.getPos().x) + ", " + ((int) self.getPos().y) + ", " + ((int) self.getPos().z) + ")"
								).getBytes(StandardCharsets.UTF_8))));
						return 1;
					}).then(argument("name", StringArgumentType.string())
							.executes(ctx -> {
								final ServerPlayerEntity self = ctx.getSource().getPlayer();
								ServerPlayNetworking.send(
										Objects.requireNonNull(self),
										new Identifier("velocity", "fabdicord"),
										new PacketByteBuf(Unpooled.wrappedBuffer(("NPOS:" + config.get("SERVER") + ":" + Objects.requireNonNull(self).getName().getString() + ":"
												+ (self.getWorld().getRegistryKey()==World.OVERWORLD?"OVERWORLD":self.getWorld().getRegistryKey()==World.NETHER?"NETHER":"END") + ":"
												+ "(" + ((int) self.getPos().x) + ", " + ((int) self.getPos().y) + ", " + ((int) self.getPos().z) + "):"
												+ StringArgumentType.getString(ctx, "name")
										).getBytes(StandardCharsets.UTF_8))));
								return 1;
							})
					)
			)
		);

		LOGGER.info("fabdicord loaded");
	}
}