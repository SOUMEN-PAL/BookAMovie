package org.devbot.bookmymovie.shared.exception;

public class UserNotFoundException extends RuntimeException {

    private final Long userId;
    private final String email;

    public UserNotFoundException(Long userId) {
        super("User not found: " + userId);
        this.userId = userId;
        this.email = null;
    }

    public UserNotFoundException(String email) {
        super("User not found: " + email);
        this.userId = null;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
