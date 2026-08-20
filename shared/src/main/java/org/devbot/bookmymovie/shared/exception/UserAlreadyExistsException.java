package org.devbot.bookmymovie.shared.exception;

public class UserAlreadyExistsException extends RuntimeException {

    private final String email;

    public UserAlreadyExistsException(String email) {
        super("User already exists: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
