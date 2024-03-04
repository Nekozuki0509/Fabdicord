package fabdicord.fabdicord;

import com.mojang.brigadier.CommandDispatcher;
import fabdicord.fabdicord.config.ModConfigs;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static fabdicord.fabdicord.config.ModConfigs.*;
import static net.minecraft.server.command.CommandManager.*;

public class Fabdicord implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabdicord");

	@Override
	public void onInitialize() {
		ModConfigs.registerConfigs();

		AdvancementCallback.EVENT.register((player, advancement) -> {
			//advancement type:server:player:title:description
			AdvancementDisplay display;
			if (!((display=advancement.getDisplay()) == null || display.isHidden()))
				ServerPlayNetworking.send(
						player,
						new Identifier("velocity", "fabdicord"),
						new PacketByteBuf(Unpooled.wrappedBuffer(("ADVANCEMENT:"+SERVER_NAME+":"+player.getName().getString()+":"+display.getTitle().getString()+":"+display.getDescription().getString())
								.getBytes(StandardCharsets.UTF_8)))
				);
		});

		ServerMessageEvents.COMMAND_MESSAGE.register((message, source, params) -> {
			//command type:server:player:command
			ServerPlayNetworking.send(
                Objects.requireNonNull(source.getPlayer()),
				new Identifier("velocity", "fabdicord"),
				new PacketByteBuf(Unpooled.wrappedBuffer(("COMMAND:"+SERVER_NAME+":"+Objects.requireNonNull(source.getPlayer()).getName()+":"+message.getContent()).getBytes(StandardCharsets.UTF_8))));
			LOGGER.info(message.getSignedContent());
			LOGGER.info(String.valueOf(message.getContent()));
		});

		//pos type:server:player:dim:x:y:z
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));

		LOGGER.info("fabdicord loaded");
	}
		public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
			dispatcher.register(literal("pos")
					.executes(ctx -> {
						final ServerPlayerEntity self = ctx.getSource().getPlayer();
						ServerPlayNetworking.send(
								Objects.requireNonNull(self),
								new Identifier("velocity", "fabdicord"),
								new PacketByteBuf(Unpooled.wrappedBuffer(("POS:" + SERVER_NAME + ":" + Objects.requireNonNull(self).getName().getString() + ":"
										+ (self.getWorld().getRegistryKey()==World.OVERWORLD?"OVERWORLD":self.getWorld().getRegistryKey()==World.NETHER?"NETHER":"END") + ":"
										+ "(" + ((int) self.getPos().x) + ", " + ((int) self.getPos().y) + ", " + ((int) self.getPos().z) + ")"
								).getBytes(StandardCharsets.UTF_8))));
						return 1;
					}));
		}
}