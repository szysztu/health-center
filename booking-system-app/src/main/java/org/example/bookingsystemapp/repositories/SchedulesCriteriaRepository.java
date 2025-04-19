package org.example.bookingsystemapp.repositories;


import org.example.bookingsystemapp.entities.Specialisation;
import org.example.bookingsystemapp.model.ScheduleCriteriaReturnTempDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SchedulesCriteriaRepository {
    List<ScheduleCriteriaReturnTempDTO> findSchedulesByCriteria(
            LocalDate startDay,
            LocalDate endDay,
            LocalTime startTime,
            LocalTime endTime,
            Specialisation specialisation
    );
}
