package fabdicord.fabdicord.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;

import static fabdicord.fabdicord.config.ModConfigs.SERVER_NAME;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("TAIL"))
    public void onDeathInject(DamageSource source, CallbackInfo callbackInfo) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        //death type:server:player:dim:xyz:message
        ServerPlayNetworking.send(
                player,
                new Identifier("velocity", "fabdicord"),
                new PacketByteBuf(Unpooled.wrappedBuffer(("DEATH:"+SERVER_NAME+":"+player.getName().getString()+":"
                        + (player.getWorld().getRegistryKey()== World.OVERWORLD?"OVERWORLD":player.getWorld().getRegistryKey()==World.NETHER?"NETHER":"END") + ":"
                        + "(" + ((int) player.getPos().x) + ", " + ((int) player.getPos().y) + ", " + ((int) player.getPos().z) + ")" + ":" + source.getDeathMessage(player).getString())
                        .getBytes(StandardCharsets.UTF_8))));
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void blockChat(SentMessage message, boolean filterMaskEnabled, MessageType.Parameters params, CallbackInfo ci) {
        ci.cancel();
    }
}
