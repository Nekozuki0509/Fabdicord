package com.github.nekozuki0509.fabdicord.mixin;

import com.github.nekozuki0509.fabdicord.impl.MinecraftApiImpl;
import net.minecraft.advancement.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.advancement.AdvancementFrame.*;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    @Shadow
    public abstract AdvancementProgress getProgress(AdvancementEntry advancement);

    @Inject(method = "grantCriterion",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public void onAdvancement(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (!getProgress(advancement).isDone()
                || advancement.value().display().isEmpty()
                || !advancement.value().display().get().shouldAnnounceToChat()) return;

        AdvancementDisplay display = advancement.value().display().get();
        boolean isChallenge = display.getFrame() == CHALLENGE;
        String frameName = display.getFrame() == TASK ? "進捗"
                : display.getFrame() == GOAL ? "目標" : "挑戦";

        MinecraftApiImpl.INSTANCE.firePlayerAdvancement(
                owner,
                display.getTitle().getString(),
                display.getDescription().getString(),
                frameName,
                isChallenge ? "dark_purple" : "green",
                isChallenge ? "完了" : "達成"
        );
    }
}