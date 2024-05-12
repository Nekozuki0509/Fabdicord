package fabdicord.fabdicord.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static fabdicord.fabdicord.Fabdicord.config;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("TAIL"))
    public void onDeathInject(DamageSource source, CallbackInfo callbackInfo) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        //death type:server:player:dim:xyz:message
        ServerPlayNetworking.send(
                player,
                new Identifier("velocity", "fabdicord"),
                new PacketByteBuf(Unpooled.wrappedBuffer(("DEATH:" + config.get("SERVER") + ":" + player.getName().getString() + ":"
                        + (player.getWorld().getRegistryKey() == World.OVERWORLD ? "OVERWORLD" : player.getWorld().getRegistryKey() == World.NETHER ? "NETHER" : "END") + ":"
                        + "(" + ((int) player.getPos().x) + ", " + ((int) player.getPos().y) + ", " + ((int) player.getPos().z) + ")" + ":" + source.getDeathMessage(player).getString())
                        .getBytes(StandardCharsets.UTF_8))));
    }

    @Inject(method = "sendMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    private void blockChat(Text message, MessageType type, UUID sender, CallbackInfo ci) {
        if (type == MessageType.CHAT) ci.cancel();
    }
}
