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

/** Loads config from AWS, binds the channel, loads {@link DiscordBotPlugin} implementations, starts JDA. */
public final class BotBootstrap {

  public static void main(String[] args) throws InterruptedException {
    var config = BotConfiguration.load();

    var plugins = new ArrayList<DiscordBotPlugin>();
    ServiceLoader.load(DiscordBotPlugin.class).forEach(plugins::add);
    if (plugins.isEmpty()) {
      plugins.add(new MinimalDeploymentPlugin());
    }

    List<BiConsumer<JDA, ChannelScope>> afterReady = new ArrayList<>();
    for (var p : plugins) {
      afterReady.add(p::afterChannelScopeReady);
    }
    var scope = new ChannelScope(config, afterReady);

    var intents = EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
    plugins.forEach(p -> intents.addAll(p.extraIntents()));

    var builder =
        JDABuilder.createDefault(config.discordToken()).enableIntents(intents).addEventListeners(scope);

    for (var plugin : plugins) {
      builder.addEventListeners(plugin.createListeners(scope, config).toArray());
    }

    builder.build().awaitReady();
  }
}
