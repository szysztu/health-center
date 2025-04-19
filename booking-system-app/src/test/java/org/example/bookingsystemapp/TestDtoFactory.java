package org.example.bookingsystemapp;

import org.example.bookingsystemapp.entities.ConfirmationMethod;
import org.example.bookingsystemapp.entities.Specialisation;
import org.example.bookingsystemapp.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.example.bookingsystemapp.entities.ConfirmationMethod.SMS;
import static org.example.bookingsystemapp.entities.Specialisation.CARDIOLOGIST;


public class TestDtoFactory {
    public PatientCreateDTO patientCreateDTO(String firstName, String lastName, String email, String phone, LocalDate birthDate, ConfirmationMethod confirmationMethod) {
        PatientCreateDTO dto = new PatientCreateDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhoneNumber(phone);
        dto.setBirthDate(birthDate);
        dto.setConfirmationMethod(confirmationMethod != null ? confirmationMethod.toString() : null);
        return dto;
    }

    public PatientCreateDTO createPatientCreateDTO() {
        return patientCreateDTO("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1995, 7, 17), SMS);
    }

    public PatientDTO patientDTO(Long id, String firstName, String lastName, String email, String phone, LocalDate birthDate, ConfirmationMethod confirmationMethod, Integer version) {
        PatientDTO dto = new PatientDTO();
        dto.setId(id);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhoneNumber(phone);
        dto.setBirthDate(birthDate);
        dto.setConfirmationMethod(confirmationMethod.toString());
        dto.setVersion(version);
        return dto;
    }

    public PatientDTO createPatientDTO(Long id, Integer version) {
        return patientDTO(id, "Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1995, 7, 17), SMS, version);
    }


    public DoctorCreateDTO doctorCreateDTO(String firstName, String lastName, String email, String phone, LocalDate birthDate, Specialisation specialisation) {
        DoctorCreateDTO dto = new DoctorCreateDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhoneNumber(phone);
        dto.setBirthDate(birthDate);
        dto.setSpecialisation(specialisation != null ? specialisation.toString() : null);
        return dto;
    }

    public DoctorCreateDTO createDoctorCreateDTO() {
        return doctorCreateDTO("Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), CARDIOLOGIST);
    }

    public DoctorDTO doctorDTO(Long id, String firstName, String lastName, String email, String phone, LocalDate birthDate, Specialisation specialisation, Integer version) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(id);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhoneNumber(phone);
        dto.setBirthDate(birthDate);
        dto.setSpecialisation(specialisation.toString());
        dto.setVersion(version);
        return dto;
    }

    public DoctorDTO createTestDoctorDTO(Long id, Integer version) {
        return doctorDTO(id, "Adam", "Nowak", "adam.nowak@example.com", "987654311", LocalDate.of(1985, 3, 25), CARDIOLOGIST, version);
    }

    public DoctorScheduleDTO doctorScheduleDTO(Long doctorId, LocalDate day, LocalTime startTime, boolean booked) {
        SingleTerminDTO termin = new SingleTerminDTO();
        termin.setDay(day);
        termin.setStartTime(startTime);
        termin.setBooked(booked);

        DoctorScheduleDTO dto = new DoctorScheduleDTO();
        dto.setDoctorId(doctorId);
        dto.setTermins(Collections.singletonList(termin));
        return dto;
    }

    public DoctorScheduleDTO createDoctorScheduleDTO(Long doctorId) {
        return doctorScheduleDTO(doctorId, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
    }

}
