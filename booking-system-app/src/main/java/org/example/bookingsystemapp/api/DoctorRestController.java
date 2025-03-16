package org.example.bookingsystemapp.api;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.example.bookingsystemapp.model.DeleteReferenceDTO;
import org.example.bookingsystemapp.model.DoctorCreateDTO;
import org.example.bookingsystemapp.model.DoctorDTO;
import org.example.bookingsystemapp.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorRestController implements DoctorApiDelegate {
    private final DoctorService doctorService;


    @Override
    @PermitAll
    public ResponseEntity<DoctorDTO> createDoctor(DoctorCreateDTO doctorCreateDTO) {
        DoctorDTO doctor = doctorService.createDoctor(doctorCreateDTO);
        return ResponseEntity.ok(doctor);
    }

    @Override
    @PermitAll
    public ResponseEntity<DoctorDTO> getDoctorById(Long doctorId) {
        DoctorDTO doctorDTO = doctorService.getDoctorById(doctorId);
        return ResponseEntity.ok(doctorDTO);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorDTO> updateDoctor(DoctorDTO doctorDTO) {
        doctorService.updateDoctor(doctorDTO);
        return ResponseEntity.ok(doctorDTO);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteDoctor(DeleteReferenceDTO deleteReferenceDTO) {
        doctorService.deleteDoctor(deleteReferenceDTO);
        return ResponseEntity.noContent().build();
    }

}
