package edu.moravian;

import java.time.Instant;

public class StudySession {

    private String sessionId;
    private String userId;
    private String subject;
    private Instant startTime;
    private long duration; // minutes

    public StudySession() {}

    public StudySession(String sessionId, String userId, String subject, Instant startTime) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.subject = subject;
        this.startTime = startTime;
        this.duration = 0L;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getId() {
        return "";
    }
}
