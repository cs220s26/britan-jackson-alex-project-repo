package edu.moravian.csci220.discordbot.spi;

import edu.moravian.csci220.discordbot.BotConfiguration;
import edu.moravian.csci220.discordbot.ChannelScope;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Plug-in entry point: optional startup hook + JDA listeners. Discovered via {@link
 * java.util.ServiceLoader} from {@code META-INF/services/edu.moravian.csci220.discordbot.spi.DiscordBotPlugin}.
 */
public interface DiscordBotPlugin {

  /** Merged into the JDABuilder after the default {@link GatewayIntent#GUILD_MESSAGES} and {@link
   * GatewayIntent#MESSAGE_CONTENT}. */
  default EnumSet<GatewayIntent> extraIntents() {
    return EnumSet.noneOf(GatewayIntent.class);
  }

  /**
   * Runs once {@link ChannelScope} has finished resolving the target channel on {@code Ready} (so {@link
   * ChannelScope#isBound()} is final for this session). Use for one-shot announcements; use {@link
   * #createListeners} for ongoing events.
   */
  default void afterChannelScopeReady(JDA jda, ChannelScope scope) {}

  /** Event listeners (e.g. {@link net.dv8tion.jda.api.hooks.ListenerAdapter}). */
  Collection<Object> createListeners(ChannelScope channels, BotConfiguration config);
}
