package fabdicord.fabdicord.mixin;

import carpet.patches.EntityPlayerMPFake;
import fabdicord.fabdicord.discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.advancement.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static fabdicord.fabdicord.Fabdicord.ServerName;
import static net.minecraft.advancement.AdvancementFrame.*;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    @Shadow
    public abstract AdvancementProgress getProgress(AdvancementEntry advancement);

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public void onAdvancement(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (advancement.value().display().isEmpty() || !advancement.value().display().get().shouldAnnounceToChat() || !getProgress(advancement).isDone()) return;

        AdvancementDisplay display = advancement.value().display().get();

        if (!display.shouldAnnounceToChat()) return;

        ServerPlayerEntity player = this.owner;
        String PN = player.getName().getString();
        String title = display.getTitle().getString();
        String description = display.getDescription().getString();
        String frame = display.getFrame() == TASK ? "進捗" : display.getFrame() == GOAL ? "目標" : "挑戦";
        boolean isChallenge = display.getFrame() == CHALLENGE;
        String color = isChallenge ? "dark_purple" : "green";
        String PNpre = player instanceof EntityPlayerMPFake ? "(BOT) " : "";

        discord.sendMessage(new EmbedBuilder()
                .setTitle("[" + ServerName + "] で" + frame + " [" + title + "] " + (isChallenge ? "完了" : "達成") + "しました")
                .setDescription(description)
                .setColor(Color.green)
                .setAuthor(PNpre + PN, null, "https://mc-heads.net/avatar/" + PN + ".png")
                .build(), false);

        discord.PMChannel.sendMessage("VELOCITY&SEND&<yellow><dark_green>[" + ServerName + "]</dark_green> で <aqua>[<blue>" + PNpre + "</blue>" + PN + "]</aqua> が" + frame
                + " <hover:show_text:'<" + color + ">" + title + "\n" + description + "</" + color + ">'><" + color + ">[" + title + "]</" + color + "></hover> を" + (isChallenge ? "完了" : "達成") + "しました").queue();
    }
}
