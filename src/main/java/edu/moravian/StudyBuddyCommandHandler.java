package edu.moravian;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StudyBuddyCommandHandler extends ListenerAdapter {

    private final RedisRepository repo;
    private final GroupService groupService;
    private final SessionService sessionService;
    private final LeaderboardService leaderboardService;

    public StudyBuddyCommandHandler(RedisRepository repo) {
        this.repo = repo;
        this.groupService = new GroupService(repo);
        this.sessionService = new SessionService(repo);
        this.leaderboardService = new LeaderboardService(repo);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

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
                event.getChannel()
                    .sendMessage(
                        "📚 Study Buddy Bot — track sessions, earn XP, have fun with friends, and get your study on!")
                    .queue();
                break;
            default:
                // Ignore unknown commands
                break;
        }
    }

    private String authorId(MessageReceivedEvent event) {
        return event.getAuthor().getId();
    }

    private String authorName(MessageReceivedEvent event) {
        return event.getAuthor().getName();
    }

    private void ensureUserExists(MessageReceivedEvent event) throws JsonProcessingException {
        String userId = authorId(event);
        String username = authorName(event);
        User existing = repo.getUser(userId);
        if (existing != null) {
            if (!username.equals(existing.getUsername())) {
                existing.setUsername(username);
                repo.saveUser(existing);
            }
            return;
        }
        repo.saveUser(new User(userId, username));
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
                try {
                    groupService.createGroup(name);
                    event.getChannel().sendMessage("📘 Group created: **" + name + "**").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage("⚠ Couldn't create group right now.").queue();
                }
                break;

            case "join":
                try {
                    ensureUserExists(event);
                    groupService.joinGroup(name, authorId(event), authorName(event));
                    event.getChannel().sendMessage("👥 You joined group **" + name + "**").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage("⚠ Couldn't join group right now.").queue();
                }
                break;

            case "leave":
                event.getChannel().sendMessage("👋 Leave group is not implemented yet.").queue();
                break;

            case "info":
                try {
                    Group g = repo.getGroup(name);
                    if (g == null) {
                        event.getChannel().sendMessage("⚠ No such group: **" + name + "**").queue();
                        break;
                    }
                    int members = g.getMembers() == null ? 0 : g.getMembers().size();
                    event.getChannel().sendMessage("ℹ **" + name + "** has **" + members + "** member(s).").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage("⚠ Couldn't load group info right now.").queue();
                }
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
                try {
                    ensureUserExists(event);
                    StudySession session = sessionService.startSession(authorId(event), subject);
                    if (session == null) {
                        event.getChannel()
                            .sendMessage("⚠ You already have an active session. Use `!session end`.")
                            .queue();
                        break;
                    }
                    event.getChannel().sendMessage("📚 Session started for **" + subject + "**").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage("⚠ Couldn't start a session right now.").queue();
                }
                break;

            case "end":
                try {
                    int xp = sessionService.endActiveSession(authorId(event));
                    if (xp == 0) {
                        event.getChannel().sendMessage("⚠ No active session to end.").queue();
                        break;
                    }
                    event.getChannel().sendMessage("✅ Session ended! You earned **" + xp + "** XP.").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage("⚠ Couldn't end your session right now.").queue();
                }
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
        try {
            String text = leaderboardService.getLeaderboard(group);
            event.getChannel().sendMessage(text).queue();
        } catch (JsonProcessingException e) {
            event.getChannel().sendMessage("⚠ Couldn't load the leaderboard right now.").queue();
        }
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
