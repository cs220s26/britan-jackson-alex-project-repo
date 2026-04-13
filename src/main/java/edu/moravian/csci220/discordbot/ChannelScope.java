package edu.moravian.csci220.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class ChannelScope extends ListenerAdapter {

  private final BotConfiguration config;
  private final List<BiConsumer<JDA, ChannelScope>> afterReady;
  private volatile long boundTextChannelId = -1;

  public ChannelScope(BotConfiguration config, List<BiConsumer<JDA, ChannelScope>> afterReady) {
    this.config = config;
    this.afterReady = afterReady;
  }

  public boolean isBound() {
    return boundTextChannelId >= 0;
  }

  public Optional<MessageChannel> activeTextChannel(JDA jda) {
    if (!isBound()) {
      return Optional.empty();
    }
    return Optional.ofNullable(jda.getTextChannelById(boundTextChannelId));
  }

  public boolean isTargetChannel(MessageChannel channel) {
    return isBound() && channel.getIdLong() == boundTextChannelId;
  }

  @Override
  public void onReady(ReadyEvent event) {
    var jda = event.getJDA();
    config
        .configuredChannelName()
        .ifPresent(
            name -> {
              var list = jda.getTextChannelsByName(name, true);
              if (!list.isEmpty()) {
                boundTextChannelId = list.get(0).getIdLong();
              }
            });
    System.out.println("Ready: " + jda.getSelfUser().getName());
    for (var h : afterReady) {
      h.accept(jda, this);
    }
  }
}
