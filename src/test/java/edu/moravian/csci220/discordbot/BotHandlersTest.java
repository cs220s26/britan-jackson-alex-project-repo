package edu.moravian.csci220.discordbot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BotHandlersTest {

  @Test
  void listenerIsAJdaListenerAdapter() {
    BotConfiguration cfg = new BotConfiguration("token", Optional.empty());
    ChannelScope scope = new ChannelScope(cfg, (jda, s) -> {});

    Object listener = BotHandlers.listener(scope);

    assertNotNull(listener);
    assertInstanceOf(ListenerAdapter.class, listener);
  }

  @Test
  void afterReadyWithUnboundScopeDoesNotNeedJda() {
    BotConfiguration cfg = new BotConfiguration("token", Optional.empty());
    ChannelScope scope = new ChannelScope(cfg, (jda, s) -> {});

    // No channel is bound yet, so nothing is sent — jda can be null here.
    BotHandlers.afterChannelScopeReady(null, scope);
  }
}
