package org.example.bookingsystemapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class KeycloakException extends RuntimeException {
    public KeycloakException(String message) {
        super(message);
    }
}
