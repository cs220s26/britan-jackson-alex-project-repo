package edu.moravian.csci220.discordbot.plugins;

import edu.moravian.csci220.discordbot.BotConfiguration;
import edu.moravian.csci220.discordbot.ChannelScope;
import edu.moravian.csci220.discordbot.spi.DiscordBotPlugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Collection;
import java.util.List;

/**
 * Default plug-in: proves deployment (one line in the target channel + {@code !ping} → {@code pong}).
 * Replace by editing {@code META-INF/services/edu.moravian.csci220.discordbot.spi.DiscordBotPlugin} or add
 * another implementation on a second line (both load).
 */
public final class MinimalDeploymentPlugin implements DiscordBotPlugin {

  @Override
  public void afterChannelScopeReady(JDA jda, ChannelScope scope) {
    tryAnnounce(jda, scope);
  }

  @Override
  public Collection<Object> createListeners(ChannelScope scope, BotConfiguration config) {
    return List.of(
        new ListenerAdapter() {
          @Override
          public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot() || !scope.isBound() || !scope.isTargetChannel(event.getChannel())) {
              return;
            }
            if ("!ping".equalsIgnoreCase(event.getMessage().getContentRaw().trim())) {
              event.getMessage().reply("pong").queue();
            }
          }
        });
  }

  private static void tryAnnounce(JDA jda, ChannelScope scope) {
    var chOpt = scope.activeTextChannel(jda);
    if (chOpt.isEmpty()) {
      if (scope.isBound()) {
        System.err.println(
            "The configured text channel is not visible to the bot — wrong server, or the bot was not invited.");
      }
      return;
    }
    MessageChannel ch = chOpt.get();
    ch.sendMessage("Bot is online. Type **!ping** in this channel to test.")
        .queue(
            null,
            err ->
                System.err.println(
                    "Could not send a message in #"
                        + ch.getName()
                        + ": "
                        + err.getMessage()
                        + " — grant **Send Messages** (and **View Channel**) for the bot role in that channel."));
  }
}
