package fabdicord.fabdicord.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fabdicord.fabdicord.discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Objects;

import static fabdicord.fabdicord.Fabdicord.ServerName;
import static fabdicord.fabdicord.Fabdicord.ignorecommand;


@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = ("execute"), at = @At("HEAD"))
    public void executeCommandInject(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (ignorecommand.stream().anyMatch(command::startsWith)) return;

        ServerCommandSource source = parseResults.getContext().getSource();
        String executer;
        discord.sendMessage(new EmbedBuilder()
                .setTitle("[" + ServerName + "]で[" + command + "]を実行しました")
                .setColor(Color.yellow)
                .setAuthor((executer = source.getPlayer() != null ? Objects.requireNonNull(source.getPlayer()).getName().getString() : source.getDisplayName().getString()),
                        null, "https://mc-heads.net/avatar/" + executer + ".png")
                .build(), false);
    }
}
