package fabdicord.fabdicord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class DiscordCommandSource implements CommandOutput {
    private final SlashCommandInteractionEvent event;
    private StringBuilder output = new StringBuilder("```\n");
    private long lastOutputMillis = 0L;

    public DiscordCommandSource(SlashCommandInteractionEvent event) {
        this.event = event;
        if (event != null) event.replyEmbeds(new EmbedBuilder()
                .setColor(Color.yellow)
                .setTitle("実行しています...")
                .build()
        ).queue();
    }

    @Override
    public void sendSystemMessage(Text message, UUID sender) {
        long currentOutputMillis = System.currentTimeMillis();
        if (this.output.length() > 1500) {
            this.output.append("```");
            this.event.getChannel().sendMessage(this.output.toString()).queue();
            this.output = new StringBuilder("```\n");
        } else {
            this.output.append(message.getString()).append("\n");
        }

        if (currentOutputMillis - this.lastOutputMillis > 50L) {
            (new Thread(() -> {
                (new Timer()).schedule(new TimerTask() {
                    public void run() {
                        DiscordCommandSource.this.output.append("```");
                        DiscordCommandSource.this.event.getChannel().sendMessage(DiscordCommandSource.this.output.toString()).queue();
                        DiscordCommandSource.this.output = new StringBuilder("```\n");
                    }
                }, 51L);
            })).start();
        }

        this.lastOutputMillis = currentOutputMillis;
    }

    public boolean shouldReceiveFeedback() {
        return true;
    }

    public boolean shouldTrackOutput() {
        return true;
    }

    public boolean shouldBroadcastConsoleToOps() {
        return true;
    }
}
