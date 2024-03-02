package fabdicord.fabdicord;

import fabdicord.fabdicord.config.ModConfigs;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static fabdicord.fabdicord.config.ModConfigs.*;

public class Fabdicord implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabdicord");

	@Override
	public void onInitialize() {
		ModConfigs.registerConfigs();

		//(dis)connect type:server:player
		//death type:server:player:dim:x:y:z:message
		//advancement type:server:player:title:description
		//command type:server:player:command
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerPlayNetworking.send(
				handler.player,
				new Identifier("velocity", "fabdicord"),
				new PacketByteBuf(Unpooled.wrappedBuffer(("CONNECT:"+SERVER_NAME+":"+handler.player.getName()).getBytes(StandardCharsets.UTF_8))))
		);

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ServerPlayNetworking.send(
				handler.player,
				new Identifier("velocity", "fabdicord"),
				new PacketByteBuf(Unpooled.wrappedBuffer(("DISCONNECT:"+SERVER_NAME+":"+handler.player.getName()).getBytes(StandardCharsets.UTF_8))))
		);

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof ServerPlayerEntity player)
				ServerPlayNetworking.send(
						player,
						new Identifier("velocity", "fabdicord"),
						new PacketByteBuf(Unpooled.wrappedBuffer(("DEATH:"+SERVER_NAME+":"+player.getName()+":"+player.getWorld().getDimension()+":"
								+((int) Objects.requireNonNull(damageSource.getPosition()).x)+":"+((int) damageSource.getPosition().y)+":"+((int) damageSource.getPosition().z)+":"
								+damageSource.getDeathMessage(player).getString())
								.getBytes(StandardCharsets.UTF_8)))
				);
		});

		AdvancementCallback.EVENT.register((player, advancement) -> {
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
			ServerPlayNetworking.send(
                Objects.requireNonNull(source.getPlayer()),
				new Identifier("velocity", "fabdicord"),
				new PacketByteBuf(Unpooled.wrappedBuffer(("COMMAND:"+SERVER_NAME+":"+Objects.requireNonNull(source.getPlayer()).getName()+":"+message.getContent()).getBytes(StandardCharsets.UTF_8))));
			LOGGER.info(message.getSignedContent());
			LOGGER.info(String.valueOf(message.getContent()));
		});

		LOGGER.info("fabdicord loaded");
	}
}