package de.cocondo.app.domain.idm.user;

/**
 * Domain exception indicating invalid login credentials.
 *
 * This exception is part of the IDM domain model and must
 * not be replaced by generic technical exceptions.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}