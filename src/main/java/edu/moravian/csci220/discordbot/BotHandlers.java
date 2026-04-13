package edu.moravian.csci220.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public final class BotHandlers {

  private BotHandlers() {}

  public static void afterChannelScopeReady(JDA jda, ChannelScope scope) {
    scope.activeTextChannel(jda).ifPresent(ch -> ch.sendMessage("Bot online. Try !ping.").queue());
  }

  public static ListenerAdapter listener(ChannelScope scope) {
    return new ListenerAdapter() {
      @Override
      public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !scope.isBound() || !scope.isTargetChannel(event.getChannel())) {
          return;
        }
        if ("!ping".equalsIgnoreCase(event.getMessage().getContentRaw().trim())) {
          event.getMessage().reply("pong").queue();
        }
      }
    };
  }
}
