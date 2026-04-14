package edu.moravian.csci220.discordbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

public final class BotBootstrap {

  public static void main(String[] args) throws InterruptedException {
    var cfg = BotConfiguration.load();
    var scope = new ChannelScope(cfg, BotHandlers::afterChannelScopeReady);
    var intents = EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
    JDABuilder.createDefault(cfg.discordToken())
        .enableIntents(intents)
        .addEventListeners(scope, BotHandlers.listener(scope))
        .build()
        .awaitReady();
  }
}
