package edu.moravian.csci220.discordbot;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotConfigurationSecretParsingTest {

  @Test
  void plainToken() {
    var c = BotConfiguration.fromSecretOnly("tok");
    assertEquals("tok", c.discordToken());
    assertTrue(c.configuredChannelName().isEmpty());
  }

  @Test
  void jsonTokenAndChannel() {
    var c = BotConfiguration.fromSecretOnly("{\"DISCORD_TOKEN\":\"t\",\"CHANNEL_NAME\":\"g\"}");
    assertEquals("t", c.discordToken());
    assertEquals(Optional.of("g"), c.configuredChannelName());
  }

  @Test
  void noTokenInJson() {
    assertThrows(IllegalStateException.class, () -> BotConfiguration.fromSecretOnly("{\"CHANNEL_NAME\":\"x\"}"));
  }

  @Test
  void blankSecret() {
    assertThrows(IllegalStateException.class, () -> BotConfiguration.fromSecretOnly("  "));
  }
}
