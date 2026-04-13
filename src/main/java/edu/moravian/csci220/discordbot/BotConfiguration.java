package edu.moravian.csci220.discordbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Optional;

public record BotConfiguration(String discordToken, Optional<String> configuredChannelName) {

  public static BotConfiguration load() {
    String region = Optional.ofNullable(System.getenv("AWS_REGION")).orElse("us-east-1");
    String secretName = Optional.ofNullable(System.getenv("AWS_SECRET_NAME")).orElse("220_Discord_Token");
    Parsed p = parseSecret(fetch(region, secretName));
    Optional<String> ch =
        Optional.ofNullable(System.getenv("DISCORD_CHANNEL_NAME")).filter(s -> !s.isBlank()).map(String::trim);
    if (ch.isEmpty()) {
      ch = Optional.ofNullable(System.getenv("CHANNEL_NAME")).filter(s -> !s.isBlank()).map(String::trim);
    }
    if (ch.isEmpty()) {
      ch = p.channelName;
    }
    return new BotConfiguration(p.token, ch);
  }

  /** For tests: parse secret string only (no env). */
  static BotConfiguration fromSecretOnly(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalStateException("Empty secret");
    }
    Parsed p = parseSecret(raw.trim());
    return new BotConfiguration(p.token, p.channelName);
  }

  private record Parsed(String token, Optional<String> channelName) {}

  private static String fetch(String region, String secretName) {
    try (var c =
        SecretsManagerClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()) {
      String s = c.getSecretValue(GetSecretValueRequest.builder().secretId(secretName).build()).secretString();
      if (s == null || s.isBlank()) {
        throw new IllegalStateException("Empty secret: " + secretName);
      }
      return s.trim();
    } catch (Exception e) {
      if (e instanceof RuntimeException re) {
        throw re;
      }
      throw new RuntimeException(e);
    }
  }

  private static Parsed parseSecret(String s) {
    if (!s.startsWith("{")) {
      return new Parsed(s, Optional.empty());
    }
    JsonObject j = JsonParser.parseString(s).getAsJsonObject();
    String token = null;
    for (var k : new String[] {"DISCORD_TOKEN", "discord_token", "token"}) {
      if (j.has(k) && j.get(k).isJsonPrimitive()) {
        token = j.get(k).getAsString();
        break;
      }
    }
    if (token == null) {
      throw new IllegalStateException("No token in JSON");
    }
    Optional<String> ch = Optional.empty();
    for (var k : new String[] {"DISCORD_CHANNEL_NAME", "CHANNEL_NAME"}) {
      if (j.has(k) && j.get(k).isJsonPrimitive()) {
        String n = j.get(k).getAsString().trim();
        if (!n.isEmpty()) {
          ch = Optional.of(n);
          break;
        }
      }
    }
    return new Parsed(token, ch);
  }
}
