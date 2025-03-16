package org.example.bookingsystemapp.mapper;


import org.example.bookingsystemapp.entities.Doctor;
import org.example.bookingsystemapp.entities.DoctorSchedule;
import org.example.bookingsystemapp.entities.Patient;
import org.example.bookingsystemapp.model.BookingDTO;
import org.example.bookingsystemapp.model.DoctorScheduleSingleDTO;
import org.example.bookingsystemapp.model.SingleTerminDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public abstract class SchedulesMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "patient", ignore = true)
    public abstract DoctorSchedule toDoctorSchedule(SingleTerminDTO singleTerminDTO);


    @Mapping(source = "doctor", target = "doctorId", qualifiedByName = "mapDoctorToDoctorId")
    @Mapping(source = "patient", target = "patientId", qualifiedByName = "mapPatientToPatientId")
    public abstract DoctorScheduleSingleDTO toDoctorScheduleDTO(DoctorSchedule doctorSchedule);

    @Mapping(source = "doctor", target = "doctorId", qualifiedByName = "mapDoctorToDoctorId")
    @Mapping(source = "patient", target = "patientId", qualifiedByName = "mapPatientToPatientId")
    public abstract BookingDTO toBookingDTO(DoctorSchedule doctorSchedule);

    @Named("mapDoctorToDoctorId")
    public static Long mapDoctorToDoctorId(Doctor doctor) {
        return doctor != null ? doctor.getId() : null;
    }

    @Named("mapPatientToPatientId")
    public static Long mapPatientToPatientId(Patient patient) {
        return patient != null ? patient.getId() : null;
    }

}
