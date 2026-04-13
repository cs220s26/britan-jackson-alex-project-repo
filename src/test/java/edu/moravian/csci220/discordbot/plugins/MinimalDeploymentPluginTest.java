package edu.moravian.csci220.discordbot.plugins;

import edu.moravian.csci220.discordbot.BotConfiguration;
import edu.moravian.csci220.discordbot.ChannelScope;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimalDeploymentPluginTest {

  @Test
  void oneListener() {
    var cfg = new BotConfiguration("t", Optional.empty());
    var scope = new ChannelScope(cfg, List.of());
    assertEquals(1, new MinimalDeploymentPlugin().createListeners(scope, cfg).size());
  }

  @Test
  void noExtraIntents() {
    assertTrue(new MinimalDeploymentPlugin().extraIntents().isEmpty());
  }
}
