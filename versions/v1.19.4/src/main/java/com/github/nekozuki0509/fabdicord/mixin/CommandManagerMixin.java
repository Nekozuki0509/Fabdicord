package com.github.nekozuki0509.fabdicord.mixin;

import com.github.nekozuki0509.fabdicord.impl.MinecraftApiImpl;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "execute", at = @At("HEAD"))
    public void executeCommandInject(
            ParseResults<ServerCommandSource> parseResults, String command, CallbackInfoReturnable<Integer> cir
    ) {
        MinecraftApiImpl.INSTANCE.fireCommandExecuted(parseResults.getContext().getSource(), command);
    }
}