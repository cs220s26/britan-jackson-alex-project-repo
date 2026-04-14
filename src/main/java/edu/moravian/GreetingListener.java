package edu.moravian;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GreetingListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();

        if (msg.equalsIgnoreCase("hello bot")) {
            event.getChannel().sendMessage("Hello! 👋").queue();
        }
    }
}
