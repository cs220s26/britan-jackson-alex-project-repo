package edu.moravian.csci220.discordbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Optional;

public final class DiscordBotApp extends ListenerAdapter {

  public static void main(String[] args) throws InterruptedException {
    var region = Optional.ofNullable(System.getenv("AWS_REGION")).orElse("us-east-1");
    var secretName = Optional.ofNullable(System.getenv("AWS_SECRET_NAME")).orElse("220_Discord_Token");
    String token = discordTokenFromAws(region, secretName);
    JDABuilder.createDefault(token).addEventListeners(new DiscordBotApp()).build().awaitReady();
  }

  private static String discordTokenFromAws(String regionId, String secretName) {
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
      var s = raw.trim();
      if (!s.startsWith("{")) {
        return s;
      }
      JsonObject json = JsonParser.parseString(s).getAsJsonObject();
      for (var key : new String[] {"DISCORD_TOKEN", "discord_token", "DISCORD_BOT_TOKEN", "token"}) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
          return json.get(key).getAsString();
        }
      }
      throw new IllegalStateException("No token field in JSON. Keys: " + json.keySet());
    } catch (Exception e) {
      if (e instanceof RuntimeException re) {
        throw re;
      }
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onReady(ReadyEvent event) {
    System.out.println("Ready: " + event.getJDA().getSelfUser().getName());
  }
}
