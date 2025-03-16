package org.example.bookingsystemapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidDoctorTokenException extends RuntimeException {
    public InvalidDoctorTokenException() {
        super("Wrong token! If you are a doctor, provide a valid token");
    }

    public InvalidDoctorTokenException(String message) {
        super(message);
    }
}
