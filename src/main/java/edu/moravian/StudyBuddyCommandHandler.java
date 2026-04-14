package edu.moravian;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StudyBuddyCommandHandler extends ListenerAdapter {

    public StudyBuddyCommandHandler() {

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String msg = event.getMessage().getContentRaw().trim();
        String[] parts = msg.split("\\s+");
        String command = parts[0].toLowerCase();

        switch (command) {

            /* =============================
                GROUP COMMANDS
               ============================= */
            case "!group":
                handleGroupCommand(parts, event);
                break;

            /* =============================
                SESSION COMMANDS
               ============================= */
            case "!session":
                handleSessionCommand(parts, event);
                break;

            /* =============================
                LEADERBOARD / XP COMMANDS
               ============================= */
            case "!leaderboard":
                handleLeaderboard(parts, event);
                break;

            case "!xp":
                handleXP(parts, event);
                break;

            /* =============================
                FUN / UTILITY COMMANDS
               ============================= */
            case "!help":
                sendHelp(event);
                break;

            case "!ping":
                event.getChannel().sendMessage("Pong! 🏓").queue();
                break;

            case "!about":
                event.getChannel().sendMessage("📚 Study Buddy Bot — track sessions, earn XP, have fun with friends, and get your study on!").queue();
                break;
        }
    }


    /* ===========================================================
                       GROUP COMMAND HANDLER
       =========================================================== */
    private void handleGroupCommand(String[] parts, MessageReceivedEvent event) {
        if (parts.length < 3) {
            event.getChannel().sendMessage("⚠ Usage: `!group <create|join|leave|info> <name>`").queue();
            return;
        }

        String action = parts[1].toLowerCase();
        String name = parts[2];

        switch (action) {
            case "create":
                event.getChannel().sendMessage("📘 Group created: **" + name + "**").queue();
                break;

            case "join":
                event.getChannel().sendMessage("👥 You joined group **" + name + "**").queue();
                break;

            case "leave":
                event.getChannel().sendMessage("👋 You left group **" + name + "**").queue();
                break;

            case "info":
                event.getChannel().sendMessage("ℹ Info for group **" + name + "**").queue();
                break;

            default:
                event.getChannel().sendMessage("⚠ Unknown group command.").queue();
        }
    }


    /* ===========================================================
                       SESSION COMMAND HANDLER
       =========================================================== */
    private void handleSessionCommand(String[] parts, MessageReceivedEvent event) {
        if (parts.length < 2) {
            event.getChannel().sendMessage("⚠ Usage: `!session <start|end|status|history>`").queue();
            return;
        }

        String action = parts[1].toLowerCase();

        switch (action) {
            case "start":
                if (parts.length < 3) {
                    event.getChannel().sendMessage("⚠ Usage: `!session start <subject>`").queue();
                    return;
                }
                String subject = parts[2];
                event.getChannel().sendMessage("📚 Session started for **" + subject + "**").queue();
                break;

            case "end":
                event.getChannel().sendMessage("✅ Session ended! XP awarded.").queue();
                break;

            case "status":
                event.getChannel().sendMessage("⏱ This Is currently under constuction ").queue();
                break;

            case "history":
                event.getChannel().sendMessage("⏱ This Isn't currently working ").queue();
                break;

            default:
                event.getChannel().sendMessage("⚠ Unknown session command.").queue();
        }
    }


    /* ===========================================================
                       LEADERBOARD HANDLER
       =========================================================== */
    private void handleLeaderboard(String[] parts, MessageReceivedEvent event) {
        if (parts.length < 2) {
            event.getChannel().sendMessage("⚠ Usage: `!leaderboard <group>`").queue();
            return;
        }

        String group = parts[1];
        event.getChannel().sendMessage("🏆 Leaderboard for **" + group + "**").queue();
    }

    /* ===========================================================
                       XP HANDLER
       =========================================================== */
    private void handleXP(String[] parts, MessageReceivedEvent event) {
        if (parts.length == 1) {
            event.getChannel().sendMessage("⭐ You have **0 XP** (for now!)").queue();
            return;
        }

        if (parts.length == 3 && parts[1].equalsIgnoreCase("rank")) {
            String group = parts[2];
            event.getChannel().sendMessage("📊 Your rank in **" + group + "** is undetermined").queue();
            return;
        }

        event.getChannel().sendMessage("⚠ Usage: `!xp` or `!xp rank <group>`").queue();
    }


    /* ===========================================================
                       HELP COMMAND
       =========================================================== */
    private void sendHelp(MessageReceivedEvent event) {
        String help = """
            📚 **Study Buddy Commands**

            **Groups**
            • `!group create <name>`
            • `!group join <name>`
            • `!group leave <name>`
            • `!group info <name>`

            **Study Sessions**
            • `!session start <subject>`
            • `!session end`
            • `!session status`
            • `!session history`

            **XP & Leaderboard**
            • `!xp`
            • `!xp rank <group>`
            • `!leaderboard <group>`

            **Utility**
            • `!help`
            • `!ping`
            • `!about`
            """;

        event.getChannel().sendMessage(help).queue();
    }
}
