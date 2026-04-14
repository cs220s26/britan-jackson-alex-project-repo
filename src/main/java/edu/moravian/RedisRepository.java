package edu.moravian;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface RedisRepository {
    void saveUser(User user) throws JsonProcessingException;
    User getUser(String userId) throws JsonProcessingException;

    void saveGroup(Group group) throws JsonProcessingException;
    Group getGroup(String groupName) throws JsonProcessingException;

    void saveSession(StudySession session) throws JsonProcessingException;
    StudySession getSession(String sessionId) throws JsonProcessingException;

    void setActiveSession(String userId, String sessionId);
    String getActiveSession(String userId);
    void clearActiveSession(String userId);
}
