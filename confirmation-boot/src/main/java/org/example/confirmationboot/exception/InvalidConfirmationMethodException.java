package org.example.confirmationboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidConfirmationMethodException extends RuntimeException {
    public InvalidConfirmationMethodException(String message) {
        super(message);
    }
}
