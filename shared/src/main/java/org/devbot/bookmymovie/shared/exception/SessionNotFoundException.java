package org.devbot.bookmymovie.shared.exception;

public class SessionNotFoundException extends RuntimeException {

    private final Long sessionId;

    public SessionNotFoundException() {
        super("Session not found");
        this.sessionId = null;
    }

    public SessionNotFoundException(Long sessionId) {
        super("Session not found: " + sessionId);
        this.sessionId = sessionId;
    }

    public Long getSessionId() {
        return sessionId;
    }
}
