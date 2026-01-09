package org.mehlib.marked.security;

/**
 * Exception thrown when user registration fails.
 */
public class RegistrationException extends Exception {

    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
