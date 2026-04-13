package edu.moravian.csci220.discordbot.spi;

import edu.moravian.csci220.discordbot.BotConfiguration;
import edu.moravian.csci220.discordbot.ChannelScope;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Collection;
import java.util.EnumSet;

public interface DiscordBotPlugin {

  default EnumSet<GatewayIntent> extraIntents() {
    return EnumSet.noneOf(GatewayIntent.class);
  }

  default void afterChannelScopeReady(JDA jda, ChannelScope scope) {}

  Collection<Object> createListeners(ChannelScope scope, BotConfiguration config);
}
