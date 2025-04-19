package org.example.bookingsystemapp.repositories;

import org.example.bookingsystemapp.entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<DoctorSchedule, Long>, SchedulesCriteriaRepository {

    @Query("""
            SELECT s FROM DoctorSchedule s
            WHERE s.doctor.id = :doctorId
            AND s.booked = false
            """)
    List<DoctorSchedule> findFreeSchedulesOfDoctorByDoctorId(@Param("doctorId") Long doctorId);

    @Query("""
            SELECT d.lastName FROM DoctorSchedule ds
            JOIN ds.doctor d
            WHERE ds.id = :scheduleId
            """)
    String findDoctorLastName(@Param("scheduleId") Long scheduleId);

    @Query("""
            SELECT COUNT(s) FROM DoctorSchedule s
            WHERE s.doctor.id = :doctorId
            AND s.startTime = :startTime
            AND s.day = :day
            AND (:scheduleId IS NULL OR s.id != :scheduleId)
            """)
    Long findByDoctorId(@Param("doctorId") Long doctorId,
                        @Param("startTime") LocalTime startTime,
                        @Param("day") LocalDate day,
                        @Param("scheduleId") Long scheduleId);
}
