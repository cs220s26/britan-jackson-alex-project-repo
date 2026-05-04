package edu.moravian;

import java.util.HashMap;
import java.util.Map;

/** Test-only in-memory {@link RedisRepository}. */
public class InMemoryRedisRepository implements RedisRepository {

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Group> groups = new HashMap<>();
    private final Map<String, StudySession> sessions = new HashMap<>();
    private final Map<String, String> activeSessions = new HashMap<>();

    private static final String K_USER = "user:";
    private static final String K_GROUP = "group:";
    private static final String K_SESSION = "session:";
    private static final String K_ACTIVE = "activeSession:";

    @Override
    public void saveUser(User user) {
        users.put(K_USER + user.getId(), user);
    }

    @Override
    public User getUser(String id) {
        return users.get(K_USER + id);
    }

    @Override
    public void saveGroup(Group group) {
        groups.put(K_GROUP + group.getName(), group);
    }

    @Override
    public Group getGroup(String groupName) {
        return groups.get(K_GROUP + groupName);
    }

    @Override
    public void saveSession(StudySession session) {
        sessions.put(K_SESSION + session.getSessionId(), session);
    }

    @Override
    public StudySession getSession(String sessionId) {
        return sessions.get(K_SESSION + sessionId);
    }

    @Override
    public void setActiveSession(String userId, String sessionId) {
        activeSessions.put(K_ACTIVE + userId, sessionId);
    }

    @Override
    public String getActiveSession(String userId) {
        return activeSessions.get(K_ACTIVE + userId);
    }

    @Override
    public void clearActiveSession(String userId) {
        activeSessions.remove(K_ACTIVE + userId);
    }
}
