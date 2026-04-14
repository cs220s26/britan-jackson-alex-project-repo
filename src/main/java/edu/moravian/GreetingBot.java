package edu.moravian;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import edu.moravian.RedisManager;

public class GreetingBot {


    public static void main(String[] args) throws Exception {

        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("DISCORD_TOKEN");

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("DISCORD_TOKEN environment variable not set");
        }

        JDA api = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new StudyBuddyCommandHandler(
                ))
                .build();
        api.awaitReady();
        System.out.println("StudyBuddy Bot is online!");
    }


}
