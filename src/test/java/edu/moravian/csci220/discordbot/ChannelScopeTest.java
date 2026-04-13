package edu.moravian.csci220.discordbot;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Light tests: full “bound channel” behavior needs Discord; here we only check the starting state. */
class ChannelScopeTest {

  @Test
  void newScopeIsNotBoundToAnyChannel() {
    BotConfiguration cfg = new BotConfiguration("token", Optional.empty());
    ChannelScope scope = new ChannelScope(cfg, (jda, s) -> {});

    assertFalse(scope.isBound());
  }

  @Test
  void whenNotBoundThereIsNoActiveTextChannel() {
    BotConfiguration cfg = new BotConfiguration("token", Optional.empty());
    ChannelScope scope = new ChannelScope(cfg, (jda, s) -> {});

    assertTrue(scope.activeTextChannel(null).isEmpty());
  }
}
