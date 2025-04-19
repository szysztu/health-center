package org.example.bookingsystemapp.entities;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum ConfirmationMethod {
    EMAIL,
    SMS
}
