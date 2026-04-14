package edu.moravian;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String id;
    private String username;
    private int totalXP = 0;
    private List<String> groups = new ArrayList<>();

    // Default constructor (for Jackson)
    public User() {}

    public User(String id, String username) {
        this.id = id;
        this.username = username;
        this.totalXP = 0;
        this.groups = new ArrayList<>();
    }

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }

    public List<String> getGroups() { return groups; }
    public void setGroups(List<String> groups) { this.groups = groups; }

    // Convenience helpers
    public void addXP(int xp) {
        this.totalXP += xp;
    }

    public void addGroup(String groupName) {
        if (groupName == null || groupName.isEmpty()) return;
        if (!groups.contains(groupName)) groups.add(groupName);
    }

    public boolean isMemberOf(String groupName) {
        return groups.contains(groupName);
    }
}
