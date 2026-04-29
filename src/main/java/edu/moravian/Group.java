package edu.moravian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

    private String name;
    private List<String> members;               // List of user IDs
    private Map<String, Integer> leaderboard;   // UserID → XP

    public Group() {
        this.members = new ArrayList<>();
        this.leaderboard = new HashMap<>();
    }

    public Group(String name) {
        this.name = name;
        this.members = new ArrayList<>();
        this.leaderboard = new HashMap<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public Map<String, Integer> getLeaderboard() { return leaderboard; }
    public void setLeaderboard(Map<String, Integer> leaderboard) { this.leaderboard = leaderboard; }

    // Helpers (idempotent)
    public void addMember(String userId) {
        if (!members.contains(userId)) {
            members.add(userId);
        }
    }

    public void addXP(String userId, int xp) {
        leaderboard.put(userId, leaderboard.getOrDefault(userId, 0) + xp);
    }
}
