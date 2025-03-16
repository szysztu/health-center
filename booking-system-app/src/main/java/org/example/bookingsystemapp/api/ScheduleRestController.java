package org.example.bookingsystemapp.api;

import lombok.RequiredArgsConstructor;
import org.example.bookingsystemapp.model.*;
import org.example.bookingsystemapp.service.SchedulesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleRestController implements ScheduleApiDelegate {

    private final SchedulesService schedulesService;

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> addDoctorSchedules(DoctorScheduleDTO doctorScheduleDTO) {
        schedulesService.addDoctorSchedule(doctorScheduleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<DoctorScheduleSingleDTO> getDoctorSchedule(Long id) {
        DoctorScheduleSingleDTO single = schedulesService.getScheduleById(id);
        return ResponseEntity.ok(single);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorScheduleSingleDTO> updateDoctorSchedule(DoctorScheduleSingleDTO doctorScheduleSingleDTO) {
        DoctorScheduleSingleDTO updated = schedulesService.updateSingleSchedule(doctorScheduleSingleDTO);
        return ResponseEntity.ok(updated);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteDoctorSchedule(DeleteReferenceDTO deleteReferenceDTO) {
        schedulesService.deleteSchedule(deleteReferenceDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<BookingDTO> createBooking(CreateBookingDTO createBookingDTO) {
        BookingDTO bookingDTO = schedulesService.createBooking(createBookingDTO);
        return ResponseEntity.ok(bookingDTO);
    }

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<ScheduleCriteriaReturnDTO>> getSchedulesByCriteria(ScheduleCriteriaReqDTO scheduleCriteriaReqDTO) {
        List<ScheduleCriteriaReturnDTO> schedules = schedulesService.getSchedulesByCriteria(scheduleCriteriaReqDTO);
        return ResponseEntity.ok(schedules);
    }

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<FreeSchedulesOfDoctorDTO>> getFreeSchedulesOfDoctor(Long doctorId) {
        List<FreeSchedulesOfDoctorDTO> schedules = schedulesService.getFreeSchedulesOfDoctor(doctorId);
        return ResponseEntity.ok(schedules);
    }
}
