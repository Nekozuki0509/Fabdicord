package fabdicord.fabdicord;

import fabdicord.fabdicord.config.ModConfigs;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fabdicord implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabdicord");

	@Override
	public void onInitialize() {

		ModConfigs.registerConfigs();

		//(dis)connect type:server:player
		//death type:server:player:dim:x:y:z
		//advancement type:server:player:title:description
		//command type:server:player:command
		ServerLifecycleEvents.SERVER_STARTED.register((server -> ));
		ServerLifecycleEvents.SERVER_STOPPED.register((server -> ));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> );
		ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> ));
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> );
		AdvancementCallback.EVENT.register(((player, advancement) -> ));
		ServerMessageEvents.COMMAND_MESSAGE.register(((message, source, params) -> ));

		LOGGER.info("fabdicord loaded");
	}
}