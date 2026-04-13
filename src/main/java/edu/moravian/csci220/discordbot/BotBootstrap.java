package edu.moravian.csci220.discordbot;

import edu.moravian.csci220.discordbot.plugins.MinimalDeploymentPlugin;
import edu.moravian.csci220.discordbot.spi.DiscordBotPlugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;

public final class BotBootstrap {

  public static void main(String[] args) throws InterruptedException {
    var cfg = BotConfiguration.load();
    var plugins = new ArrayList<DiscordBotPlugin>();
    ServiceLoader.load(DiscordBotPlugin.class).forEach(plugins::add);
    if (plugins.isEmpty()) {
      plugins.add(new MinimalDeploymentPlugin());
    }
    var afterReady = new ArrayList<BiConsumer<JDA, ChannelScope>>();
    for (var p : plugins) {
      afterReady.add(p::afterChannelScopeReady);
    }
    var scope = new ChannelScope(cfg, afterReady);
    var intents = EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
    plugins.forEach(p -> intents.addAll(p.extraIntents()));
    var b = JDABuilder.createDefault(cfg.discordToken()).enableIntents(intents).addEventListeners(scope);
    for (var p : plugins) {
      b.addEventListeners(p.createListeners(scope, cfg).toArray());
    }
    b.build().awaitReady();
  }
}
