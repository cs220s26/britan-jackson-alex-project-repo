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
  void createListeners_returnsOneListener() {
    var plugin = new MinimalDeploymentPlugin();
    var cfg = new BotConfiguration("t", Optional.empty());
    var scope = new ChannelScope(cfg, List.of());
    var listeners = plugin.createListeners(scope, cfg);
    assertEquals(1, listeners.size());
  }

  @Test
  void extraIntents_emptyByDefault() {
    assertTrue(new MinimalDeploymentPlugin().extraIntents().isEmpty());
  }
}
