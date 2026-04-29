package edu.moravian;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class SessionService {

    private final RedisRepository repo;
    // XP rate: 5 XP per minute
    private static final int XP_PER_MIN = 5;

    public SessionService(RedisRepository repo) {
        this.repo = repo;
    }

    public StudySession startSession(String userId, String subject) throws JsonProcessingException {
        String active = repo.getActiveSession(userId);
        if (active != null) {
            return null; // already active
        }

        String sessionId = UUID.randomUUID().toString();
        StudySession session = new StudySession(sessionId, userId, subject, Instant.now());
        repo.saveSession(session);
        repo.setActiveSession(userId, sessionId);
        return session;
    }

    public int endActiveSession(String userId) throws JsonProcessingException {
        String sessionId = repo.getActiveSession(userId);
        if (sessionId == null) {
            return 0;
        }

        StudySession session = repo.getSession(sessionId);
        if (session == null) {
            repo.clearActiveSession(userId);
            return 0;
        }

        long minutes = Duration.between(session.getStartTime(), Instant.now()).toMinutes();
        if (minutes < 0) {
            minutes = 0;
        }
        session.setDuration(minutes);
        repo.saveSession(session);
        repo.clearActiveSession(userId);

        int xp = calculateXP(minutes);
        applyXPToUser(userId, xp);

        return xp;
    }

    private int calculateXP(long minutes) {
        return (int) (minutes * XP_PER_MIN);
    }

    private void applyXPToUser(String userId, int xp) throws JsonProcessingException {
        User user = repo.getUser(userId);
        if (user == null) {
            user = new User(userId, "unknown");
        }
        user.addXP(xp);

        // update group leaderboards (simple approach)
        for (String gName : user.getGroups()) {
            Group g = repo.getGroup(gName);
            if (g != null) {
                g.addXP(userId, xp);
                repo.saveGroup(g);
            }
        }
        repo.saveUser(user);
    }
}
