package com.github.nekozuki0509.fabdicord;

import com.github.nekozuki0509.common.Common;
import com.github.nekozuki0509.fabdicord.impl.MinecraftApiImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class Fabdicord implements ModInitializer {

    @Override
    public void onInitialize() {
        Common.init(new MinecraftApiImpl());

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> false);
    }
}
