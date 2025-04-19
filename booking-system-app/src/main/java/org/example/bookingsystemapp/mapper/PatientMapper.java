package org.example.bookingsystemapp.mapper;

import org.example.bookingsystemapp.entities.Patient;
import org.example.bookingsystemapp.model.PatientCreateDTO;
import org.example.bookingsystemapp.model.PatientDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    PatientDTO toPatientDTO(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "doctorSchedule", ignore = true)
    Patient toPatient(PatientCreateDTO patientCreateDTO);
}
