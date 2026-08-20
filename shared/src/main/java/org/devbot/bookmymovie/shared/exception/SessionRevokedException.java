package org.devbot.bookmymovie.shared.exception;

public class SessionRevokedException extends RuntimeException {

    private final Long sessionId;

    public SessionRevokedException(Long sessionId) {
        super("Session is revoked: " + sessionId);
        this.sessionId = sessionId;
    }

    public Long getSessionId() {
        return sessionId;
    }
}
