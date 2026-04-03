package com.github.nekozuki0509.fabdicord.mixin;

import com.github.nekozuki0509.fabdicord.impl.MinecraftApiImpl;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("TAIL"))
    public void onDeathInject(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        var key = player.getWorld().getRegistryKey();
        String dim = key == World.OVERWORLD ? "OVERWORLD"
                : key == World.NETHER ? "NETHER" : "END";

        MinecraftApiImpl.INSTANCE.firePlayerDeath(
                player,
                source.getDeathMessage(player).getString(),
                dim,
                (int) player.getPos().x,
                (int) player.getPos().y,
                (int) player.getPos().z
        );
    }
}