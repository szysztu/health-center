package org.example.bookingsystemapp;

import jakarta.transaction.Transactional;
import org.example.bookingsystemapp.entities.*;
import org.example.bookingsystemapp.repositories.DoctorRepository;
import org.example.bookingsystemapp.repositories.PatientRepository;
import org.example.bookingsystemapp.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.example.bookingsystemapp.entities.ConfirmationMethod.SMS;
import static org.example.bookingsystemapp.entities.Specialisation.CARDIOLOGIST;

@Component
@Transactional
public class TestEntityFactory {
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public Doctor createDoctor(String firstName, String lastName, String email, String phone, LocalDate birthDate, Specialisation specialisation) {
        return doctorRepository.save(Doctor.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phone)
                .birthDate(birthDate)
                .specialisation(specialisation)
                .build());
    }

    public Doctor createTestDoctor() {
        return createDoctor("Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), CARDIOLOGIST);
    }

    public Patient createPatient(String firstName, String lastName, String email, String phone, LocalDate birthDate, ConfirmationMethod confirmationMethod) {
        return patientRepository.save(Patient.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phone)
                .birthDate(birthDate)
                .confirmationMethod(confirmationMethod)
                .build());
    }

    public Patient createTestPatient() {
        return createPatient("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1995, 7, 17), SMS);
    }

    public DoctorSchedule createSchedule(Doctor doctor, LocalDate day, LocalTime startTime, boolean booked) {
        return scheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .day(day)
                .startTime(startTime)
                .booked(booked)
                .build());
    }

}
