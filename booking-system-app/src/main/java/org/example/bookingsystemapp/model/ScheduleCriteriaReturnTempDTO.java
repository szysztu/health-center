package org.example.bookingsystemapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.bookingsystemapp.entities.Specialisation;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class ScheduleCriteriaReturnTempDTO {
    private Long doctorId;
    private String doctorLastName;
    private Specialisation specialisation;
    private LocalDate day;
    private LocalTime startTime;
    private boolean booked;
}
