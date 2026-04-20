package edu.moravian.csci220.discordbot;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests secret parsing via {@link BotConfiguration#fromSecretOnly(String)} (no AWS). Production
 * {@link BotConfiguration#load()} fetches the secret from AWS; channel name comes only from the
 * secret value (JSON), not from the process environment.
 */
class BotConfigurationSecretParsingTest {

  @Test
  void plainTextSecretIsJustTheToken() {
    BotConfiguration c = BotConfiguration.fromSecretOnly("my-token-here");
    assertEquals("my-token-here", c.discordToken());
    assertTrue(c.configuredChannelName().isEmpty());
  }

  @Test
  void plainTextIsTrimmed() {
    BotConfiguration c = BotConfiguration.fromSecretOnly("  abc  ");
    assertEquals("abc", c.discordToken());
  }

  @Test
  void jsonCanUseDiscordTokenKey() {
    String json = "{\"DISCORD_TOKEN\":\"hello\",\"CHANNEL_NAME\":\"general\"}";
    BotConfiguration c = BotConfiguration.fromSecretOnly(json);
    assertEquals("hello", c.discordToken());
    assertEquals(Optional.of("general"), c.configuredChannelName());
  }

  @Test
  void jsonCanUseLowercaseDiscordTokenKey() {
    String json = "{\"discord_token\":\"lowercase-key\"}";
    BotConfiguration c = BotConfiguration.fromSecretOnly(json);
    assertEquals("lowercase-key", c.discordToken());
  }

  @Test
  void jsonCanUseShortTokenKey() {
    String json = "{\"token\":\"short\"}";
    BotConfiguration c = BotConfiguration.fromSecretOnly(json);
    assertEquals("short", c.discordToken());
  }

  @Test
  void jsonDiscordChannelNameWinsOverChannelName() {
    String json =
        "{\"DISCORD_TOKEN\":\"t\",\"DISCORD_CHANNEL_NAME\":\"first\",\"CHANNEL_NAME\":\"second\"}";
    BotConfiguration c = BotConfiguration.fromSecretOnly(json);
    assertEquals(Optional.of("first"), c.configuredChannelName());
  }

  @Test
  void jsonWithOnlyChannelNameAndNoTokenThrows() {
    assertThrows(
        IllegalStateException.class,
        () -> BotConfiguration.fromSecretOnly("{\"CHANNEL_NAME\":\"oops\"}"));
  }

  @Test
  void jsonWithEmptyObjectThrows() {
    assertThrows(IllegalStateException.class, () -> BotConfiguration.fromSecretOnly("{}"));
  }

  @Test
  void blankSecretThrows() {
    assertThrows(IllegalStateException.class, () -> BotConfiguration.fromSecretOnly("   "));
  }

  @Test
  void nullSecretThrows() {
    assertThrows(IllegalStateException.class, () -> BotConfiguration.fromSecretOnly(null));
  }
}
