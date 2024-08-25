package fabdicord.fabdicord.mixin;

import carpet.patches.EntityPlayerMPFake;
import fabdicord.fabdicord.discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static fabdicord.fabdicord.Fabdicord.ServerName;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("TAIL"))
    public void onDeathInject(DamageSource source, CallbackInfo callbackInfo) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        String PN = player.getName().getString();
        String place = (player.getWorld().getRegistryKey() == World.OVERWORLD ? "OVERWORLD" : player.getWorld().getRegistryKey() == World.NETHER ? "NETHER" : "END") + ":"
                + "(" + ((int) player.getPos().x) + ", " + ((int) player.getPos().y) + ", " + ((int) player.getPos().z) + ")";
        boolean isbot = player instanceof EntityPlayerMPFake;
        String PNpre = isbot ? "(BOT) " : "";
        String message = source.getDeathMessage(player).getString();
        discord.sendMessage(new EmbedBuilder()
                .setTitle("[" + ServerName + "] の [" + place + "] で死亡しました")
                .setDescription(message)
                .setColor(Color.red)
                .setAuthor(PNpre + PN, null, "https://mc-heads.net/avatar/" + PN + ".png")
                .build(), false);

        discord.PMChannel.sendMessage("VELOCITY&READ&<hover:show_text:'" + message + "'><red><dark_green>[" + ServerName + "]</dark_green><yellow>" + place
                + "</yellow> で <aqua>[<blue>" + PNpre + "</blue>" + PN + "]</aqua> が死亡しました&" + ServerName + place + "で" + PNpre + PN + "が死亡しました").queue();
    }
}
