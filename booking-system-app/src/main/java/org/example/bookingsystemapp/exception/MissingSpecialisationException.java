package org.example.bookingsystemapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MissingSpecialisationException extends RuntimeException {
    public MissingSpecialisationException(String message) {
        super(message);
    }
}
