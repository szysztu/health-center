package org.example.bookingsystemapp.service.help;

import lombok.RequiredArgsConstructor;
import org.example.bookingsystemapp.entities.DoctorSchedule;
import org.example.bookingsystemapp.exception.InvalidDataException;
import org.example.bookingsystemapp.exception.InvalidScheduleTime;
import org.example.bookingsystemapp.repositories.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ScheduleValidator {

    private final ScheduleRepository scheduleRepository;
    private final static LocalTime OPENING_TIME = LocalTime.of(10, 0);
    private final static LocalTime CLOSING_TIME = LocalTime.of(20, 0);

    public void validateStartTimeAndDayOfSchedule(LocalTime startTime, LocalDate day, DoctorSchedule schedule) {
        if (startTime != null) {
            validateStartTimeOfSchedule(startTime);
        }
        validateIfScheduleAlreadyExists(
                schedule.getDoctor().getId(),
                ofNullable(startTime).orElse(schedule.getStartTime()),
                ofNullable(day).orElse(schedule.getDay()),
                schedule.getId());
    }

    public void validateStartTimeOfSchedule(LocalTime startTime) {
        if (startTime.isBefore(OPENING_TIME) || startTime.isAfter(CLOSING_TIME)) {
            throw new InvalidScheduleTime("Start time must be between 10:00 and 20:00");
        }
        if (startTime.getMinute() % 30 != 0) {
            throw new InvalidScheduleTime("Schedule must start every 30 minutes, for example: 10:00, 10:30, 11:00");
        }
    }

    public void validateIfScheduleAlreadyExists(Long doctorId, LocalTime startTime, LocalDate day, Long scheduleId) {
        var amount = scheduleRepository.findByDoctorId(doctorId, startTime, day, scheduleId);

        if (amount != null && amount > 0) {
            throw new InvalidDataException("Doctor already has a schedule at %s on %s".formatted( startTime,day));
        }
    }

}
