package de.cocondo.app.system.core.http;

public class InvalidHttpHeaderException extends RuntimeException {

    public InvalidHttpHeaderException(String message) {
        super(message);
    }

}
