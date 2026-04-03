package com.github.nekozuki0509.fabdicord;

import com.github.nekozuki0509.common.Common;
import com.github.nekozuki0509.fabdicord.impl.MinecraftApiImpl;
import com.github.nekozuki0509.fabdicord.impl.MinecraftServerApiImpl;
import net.fabricmc.api.ModInitializer;

public class Fabdicord implements ModInitializer {

    @Override
    public void onInitialize() {
        Common.init(new MinecraftApiImpl());
    }
}
