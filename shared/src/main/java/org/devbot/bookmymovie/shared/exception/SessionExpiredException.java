package org.devbot.bookmymovie.shared.exception;

public class SessionExpiredException extends RuntimeException {

    private final Long sessionId;

    public SessionExpiredException(Long sessionId) {
        super("Session is expired: " + sessionId);
        this.sessionId = sessionId;
    }

    public Long getSessionId() {
        return sessionId;
    }
}
