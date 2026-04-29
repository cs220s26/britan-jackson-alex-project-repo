package edu.moravian;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LeaderboardService {

    private final RedisRepository repo;

    public LeaderboardService(RedisRepository repo) {
        this.repo = repo;
    }

    public String getLeaderboard(String groupName) throws JsonProcessingException {
        Group g = repo.getGroup(groupName);
        if (g == null) {
            return "No such group: " + groupName;
        }

        Map<String, Integer> board = g.getLeaderboard();
        if (board == null || board.isEmpty()) {
            return "No activity yet for group: " + groupName;
        }

        List<Map.Entry<String, Integer>> sorted = board.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("Leaderboard for ").append(groupName).append(":\n");
        int place = 1;
        for (Map.Entry<String, Integer> e : sorted) {
            String userId = e.getKey();
            int xp = e.getValue();
            String username = Optional.ofNullable(repo.getUser(userId)).map(User::getUsername).orElse(userId);
            sb.append(place).append(". ").append(username).append(" — ").append(xp).append(" XP\n");
            place++;
        }
        return sb.toString();
    }
}
