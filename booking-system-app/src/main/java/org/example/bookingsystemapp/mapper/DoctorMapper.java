package org.example.bookingsystemapp.mapper;

import org.example.bookingsystemapp.entities.Doctor;
import org.example.bookingsystemapp.model.DoctorCreateDTO;
import org.example.bookingsystemapp.model.DoctorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    DoctorDTO toDoctorDTO(Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "specialisation", ignore = true)
    @Mapping(target = "doctorSchedule", ignore = true)
    Doctor toDoctor(DoctorCreateDTO doctorCreateDTO);


}
