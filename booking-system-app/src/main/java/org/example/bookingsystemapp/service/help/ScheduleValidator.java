package org.example.bookingsystemapp.service.help;

import lombok.RequiredArgsConstructor;
import org.example.bookingsystemapp.entities.DoctorSchedule;
import org.example.bookingsystemapp.exception.InvalidDataException;
import org.example.bookingsystemapp.exception.InvalidScheduleTime;
import org.example.bookingsystemapp.repositories.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ScheduleValidator {

    private final ScheduleRepository scheduleRepository;
    private final static LocalTime OPENING_TIME = LocalTime.of(10, 0);
    private final static LocalTime CLOSING_TIME = LocalTime.of(20, 0);

    public void validateStartTimeAndDay(LocalTime startTime, LocalDate day, DoctorSchedule schedule) {
        if (startTime != null && day == null) {
            validateStartTime(startTime);
            validateIfExists(schedule.getDoctor().getId(), startTime, schedule.getDay(), schedule.getId());
        } else if (startTime == null && day != null) {
            validateIfExists(schedule.getDoctor().getId(), schedule.getStartTime(), day, schedule.getId());
        } else if (startTime != null && day != null) {
            validateStartTime(startTime);
            validateIfExists(schedule.getDoctor().getId(), startTime, day, schedule.getId());
        }
    }

    public void validateStartTime(LocalTime startTime) {
        if (startTime.isBefore(OPENING_TIME) || startTime.isAfter(CLOSING_TIME)) {
            throw new InvalidScheduleTime("Start time must be between 10:00 and 20:00");
        }
        if (startTime.getMinute() % 30 != 0) {
            throw new InvalidScheduleTime("Schedule must start every 30 minutes, for example: 10:00, 10:30, 11:00");
        }
    }

    public void validateIfExists(Long doctorId, LocalTime startTime, LocalDate day, Long scheduleId) {
        var amount = scheduleRepository.existsByDoctorId(doctorId, startTime, day, scheduleId);

        if (amount != null && amount > 0) {
            throw new InvalidDataException("Doctor already has a schedule at " + startTime + " on " + day);
        }
    }

}
