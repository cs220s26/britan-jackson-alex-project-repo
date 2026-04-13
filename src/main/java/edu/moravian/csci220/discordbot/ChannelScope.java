package edu.moravian.csci220.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Resolves the configured Discord text channel by name once JDA is ready, stores that channel’s id
 * internally for matching and lookups. Plugins use {@link #isTargetChannel} and {@link #isBound()}.
 */
public final class ChannelScope extends ListenerAdapter {

  private final BotConfiguration config;
  private final List<BiConsumer<JDA, ChannelScope>> afterReady;
  /**
   * Discord text channel id for the resolved name, set in {@link #onReady}; {@code -1} if unbound.
   * Not part of configuration — captured from JDA when the name is resolved.
   */
  private volatile long boundTextChannelId = -1;

  public ChannelScope(BotConfiguration config, List<BiConsumer<JDA, ChannelScope>> afterReady) {
    this.config = config;
    this.afterReady = afterReady;
  }

  public boolean isBound() {
    return boundTextChannelId >= 0;
  }

  /**
   * The bound text channel, if JDA can see it. Uses the id captured when the configured channel name
   * was resolved on ready.
   */
  public Optional<MessageChannel> activeTextChannel(JDA jda) {
    return optionalBoundTextChannel(jda);
  }

  private Optional<MessageChannel> optionalBoundTextChannel(JDA jda) {
    if (!isBound()) {
      return Optional.empty();
    }
    return Optional.ofNullable(jda.getTextChannelById(boundTextChannelId));
  }

  /** Same rule as {@link #isTargetChannel(MessageChannel)}. */
  boolean matchesBoundChannel(long channelId) {
    return isBound() && channelId == boundTextChannelId;
  }

  public boolean isTargetChannel(MessageChannel channel) {
    return matchesBoundChannel(channel.getIdLong());
  }

  @Override
  public void onReady(ReadyEvent event) {
    var jda = event.getJDA();
    var botName = jda.getSelfUser().getName();
    if (config.configuredChannelName().isPresent()) {
      var name = config.configuredChannelName().get();
      var matches = jda.getTextChannelsByName(name, true);
      if (matches.isEmpty()) {
        System.err.println(
            "No text channel named \"" + name + "\". Check spelling and that the bot can see that server/channel.");
      } else {
        boundTextChannelId = matches.get(0).getIdLong();
        if (matches.size() > 1) {
          var ch = matches.get(0);
          System.err.println(
              "Warning: "
                  + matches.size()
                  + " channels named \""
                  + name
                  + "\"; using #"
                  + ch.getName()
                  + " in "
                  + ch.getGuild().getName());
        }
        System.out.println("Ready: " + botName + " — text channel #" + matches.get(0).getName());
      }
    } else {
      System.out.println(
          "Ready: "
              + botName
              + " — no channel filter (set CHANNEL_NAME / DISCORD_CHANNEL_NAME in env or secret JSON)");
    }
    for (var hook : afterReady) {
      hook.accept(jda, this);
    }
  }
}
