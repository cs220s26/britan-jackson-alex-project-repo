package edu.moravian.csci220.discordbot.plugins;

import edu.moravian.csci220.discordbot.BotConfiguration;
import edu.moravian.csci220.discordbot.ChannelScope;
import edu.moravian.csci220.discordbot.spi.DiscordBotPlugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Collection;
import java.util.List;

public final class MinimalDeploymentPlugin implements DiscordBotPlugin {

  @Override
  public void afterChannelScopeReady(JDA jda, ChannelScope scope) {
    scope
        .activeTextChannel(jda)
        .ifPresent(ch -> ch.sendMessage("Bot online. Try !ping.").queue());
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
}
