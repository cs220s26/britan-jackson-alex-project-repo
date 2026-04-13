package edu.moravian.csci220.discordbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Optional;

/** Token + optional channel name from AWS Secrets Manager and environment (merged). */
public record BotConfiguration(String discordToken, Optional<String> configuredChannelName) {

  public static BotConfiguration load() {
    var region = Optional.ofNullable(System.getenv("AWS_REGION")).orElse("us-east-1");
    var secretName = Optional.ofNullable(System.getenv("AWS_SECRET_NAME")).orElse("220_Discord_Token");
    var loaded = loadSecretFromAws(region, secretName);
    var fromEnvName = channelNameFromEnv();
    Optional<String> channelName = fromEnvName.isPresent() ? fromEnvName : loaded.channelName();
    if (channelName.isEmpty()) {
      System.err.println(
          "Set CHANNEL_NAME or DISCORD_CHANNEL_NAME (env), or put CHANNEL_NAME / DISCORD_CHANNEL_NAME in the secret JSON.");
    }
    return new BotConfiguration(loaded.token(), channelName);
  }

  private record LoadedSecret(String token, Optional<String> channelName) {}

  /** Parses a Secrets Manager payload (plain token or JSON) without reading environment. */
  static BotConfiguration parseSecretWithoutEnvMerge(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalStateException("Empty secret");
    }
    var loaded = parseSecretPayload(raw.trim());
    return new BotConfiguration(loaded.token(), loaded.channelName());
  }

  private static LoadedSecret parseSecretPayload(String s) {
    if (!s.startsWith("{")) {
      return new LoadedSecret(s, Optional.empty());
    }
    JsonObject json = JsonParser.parseString(s).getAsJsonObject();
    String token = null;
    for (var key : new String[] {"DISCORD_TOKEN", "discord_token", "DISCORD_BOT_TOKEN", "token"}) {
      if (json.has(key) && json.get(key).isJsonPrimitive()) {
        token = json.get(key).getAsString();
        break;
      }
    }
    if (token == null) {
      throw new IllegalStateException("No token field in JSON. Keys: " + json.keySet());
    }
    return new LoadedSecret(token, channelNameFromJson(json));
  }

  private static LoadedSecret loadSecretFromAws(String regionId, String secretName) {
    try (var client =
        SecretsManagerClient.builder()
            .region(Region.of(regionId))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()) {
      var raw =
          client
              .getSecretValue(GetSecretValueRequest.builder().secretId(secretName).build())
              .secretString();
      if (raw == null || raw.isBlank()) {
        throw new IllegalStateException("Empty secret: " + secretName);
      }
      return parseSecretPayload(raw.trim());
    } catch (Exception e) {
      if (e instanceof RuntimeException re) {
        throw re;
      }
      throw new RuntimeException(e);
    }
  }

  private static Optional<String> channelNameFromEnv() {
    return firstNonBlankEnv("DISCORD_CHANNEL_NAME", "CHANNEL_NAME")
        .map(String::trim)
        .filter(s -> !s.isEmpty());
  }

  private static Optional<String> firstNonBlankEnv(String a, String b) {
    var x = System.getenv(a);
    if (x != null && !x.isBlank()) {
      return Optional.of(x);
    }
    x = System.getenv(b);
    if (x != null && !x.isBlank()) {
      return Optional.of(x);
    }
    return Optional.empty();
  }

  private static Optional<String> channelNameFromJson(JsonObject json) {
    for (var key : new String[] {"DISCORD_CHANNEL_NAME", "CHANNEL_NAME", "channel_name", "channel"}) {
      if (!json.has(key)) {
        continue;
      }
      var el = json.get(key);
      if (el.isJsonNull() || !el.isJsonPrimitive()) {
        continue;
      }
      var n = el.getAsString().trim();
      if (!n.isEmpty()) {
        return Optional.of(n);
      }
    }
    return Optional.empty();
  }
}
