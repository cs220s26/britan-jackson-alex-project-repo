package edu.moravian;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardServiceTest {

    @Test
    void testLeaderboardSorted() throws JsonProcessingException {
        FakeRedisRepository repo = new FakeRedisRepository();

        Group g = new Group("coding");
        g.addXP("u1", 10);
        g.addXP("u2", 30);
        g.addXP("u3", 20);

        repo.saveGroup(g);

        repo.saveUser(new User("u1", "Alex"));
        repo.saveUser(new User("u2", "Chris"));
        repo.saveUser(new User("u3", "Barrie"));

        LeaderboardService lb = new LeaderboardService(repo);

        String text = lb.getLeaderboard("coding");

        // Ensure order: u2 > u3 > u1
        assertTrue(text.indexOf("Chris") < text.indexOf("Barrie"));
        assertTrue(text.indexOf("Barrie") < text.indexOf("Alex"));
    }
}
