package edu.moravian.csci220.discordbot;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BotHandlersTest {

  @Test
  void listenerCreated() {
    var cfg = new BotConfiguration("t", Optional.empty());
    var scope = new ChannelScope(cfg, (jda, s) -> {});
    assertNotNull(BotHandlers.listener(scope));
  }
}
