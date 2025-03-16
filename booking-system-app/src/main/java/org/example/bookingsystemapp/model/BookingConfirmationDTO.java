package org.example.bookingsystemapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmationDTO {
    private String patientEmail;
    private LocalDate scheduleDay;
    private LocalTime scheduleHour;
    private String doctorName;
    private String confirmationMethod;
    private String phoneNumber;
}
