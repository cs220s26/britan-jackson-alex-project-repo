package edu.moravian;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SessionServiceTest {

    @Test
    void testStartSession() throws JsonProcessingException {
        InMemoryRedisRepository repo = new InMemoryRedisRepository();
        SessionService service = new SessionService(repo);

        StudySession s = service.startSession("u1", "math");

        assertNotNull(s);
        assertEquals("math", s.getSubject());
        assertEquals(s.getSessionId(), repo.getActiveSession("u1"));
    }

    @Test
    void testEndSessionAwardsXP() throws JsonProcessingException {
        InMemoryRedisRepository repo = new InMemoryRedisRepository();
        SessionService service = new SessionService(repo);

        // Create user + group
        User u = new User("u1", "alex");
        u.addGroup("math");
        repo.saveUser(u);

        Group g = new Group("math");
        repo.saveGroup(g);

        // Start session
        StudySession s = new StudySession("s1", "u1", "math", Instant.now().minusSeconds(120));
        repo.saveSession(s);
        repo.setActiveSession("u1", "s1");

        int xp = service.endActiveSession("u1");

        // 120 sec = 2 minutes → 2 * 5 XP = 10 XP
        assertEquals(10, xp);

        // User updated
        User updated = repo.getUser("u1");
        assertEquals(10, updated.getTotalXP());

        // Group leaderboard updated
        assertEquals(10, repo.getGroup("math").getLeaderboard().get("u1"));
    }
}
