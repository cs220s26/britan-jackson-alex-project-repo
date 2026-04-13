package edu.moravian.csci220.discordbot;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotConfigurationSecretParsingTest {

  @Test
  void plainTokenString_parsesAsTokenOnly() {
    var c = BotConfiguration.parseSecretWithoutEnvMerge("my-bot-token");
    assertEquals("my-bot-token", c.discordToken());
    assertTrue(c.configuredChannelName().isEmpty());
  }

  @Test
  void jsonWithDiscordToken_andChannelName() {
    var json = "{\"DISCORD_TOKEN\":\"t\",\"CHANNEL_NAME\":\"general\"}";
    var c = BotConfiguration.parseSecretWithoutEnvMerge(json);
    assertEquals("t", c.discordToken());
    assertEquals(Optional.of("general"), c.configuredChannelName());
  }

  @Test
  void jsonIgnoresLegacyChannelIdKeys() {
    var json =
        "{\"DISCORD_TOKEN\":\"t\",\"CHANNEL_ID\":\"987654321098765432\",\"CHANNEL_NAME\":\"general\"}";
    var c = BotConfiguration.parseSecretWithoutEnvMerge(json);
    assertEquals("t", c.discordToken());
    assertEquals(Optional.of("general"), c.configuredChannelName());
  }

  @Test
  void jsonMissingToken_throws() {
    var json = "{\"CHANNEL_NAME\":\"x\"}";
    assertThrows(IllegalStateException.class, () -> BotConfiguration.parseSecretWithoutEnvMerge(json));
  }

  @Test
  void emptySecret_throws() {
    assertThrows(IllegalStateException.class, () -> BotConfiguration.parseSecretWithoutEnvMerge("   "));
  }
}
