package edu.moravian;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisManager implements RedisRepository {

    private static final String K_USER = "user:";
    private static final String K_GROUP = "group:";
    private static final String K_SESSION = "session:";
    private static final String K_ACTIVE = "activeSession:";

    private final ObjectMapper mapper;
    private Jedis jedis;

    public RedisManager() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            jedis = new Jedis("localhost", 6379);
            System.out.println("Redis connection successful: " + jedis.ping());
        } catch (JedisConnectionException e) {
            System.out.println("Could not successfully connect to Redis Server");
            jedis = null;
        }
    }

    private boolean connected() {
        return jedis != null;
    }

    @Override
    public void saveUser(User user) throws JsonProcessingException {
        if (!connected() || user == null) return;
        jedis.set(K_USER + user.getId(), mapper.writeValueAsString(user));
    }

    @Override
    public User getUser(String userId) throws JsonProcessingException {
        if (!connected()) return null;
        String json = jedis.get(K_USER + userId);
        return json == null ? null : mapper.readValue(json, User.class);
    }

    @Override
    public void saveGroup(Group group) throws JsonProcessingException {
        if (!connected() || group == null) return;
        jedis.set(K_GROUP + group.getName(), mapper.writeValueAsString(group));
    }

    @Override
    public Group getGroup(String groupName) throws JsonProcessingException {
        if (!connected()) return null;
        String json = jedis.get(K_GROUP + groupName);
        return json == null ? null : mapper.readValue(json, Group.class);
    }

    @Override
    public void saveSession(StudySession session) throws JsonProcessingException {
        if (!connected() || session == null) return;
        jedis.set(K_SESSION + session.getSessionId(), mapper.writeValueAsString(session));
    }

    @Override
    public StudySession getSession(String sessionId) throws JsonProcessingException {
        if (!connected()) return null;
        String json = jedis.get(K_SESSION + sessionId);
        return json == null ? null : mapper.readValue(json, StudySession.class);
    }

    @Override
    public void setActiveSession(String userId, String sessionId) {
        if (!connected()) return;
        jedis.set(K_ACTIVE + userId, sessionId);
    }

    @Override
    public String getActiveSession(String userId) {
        if (!connected()) return null;
        return jedis.get(K_ACTIVE + userId);
    }

    @Override
    public void clearActiveSession(String userId) {
        if (!connected()) return;
        jedis.del(K_ACTIVE + userId);
    }

    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}