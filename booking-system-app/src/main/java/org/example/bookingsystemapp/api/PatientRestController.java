package org.example.bookingsystemapp.api;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.example.bookingsystemapp.model.DeleteReferenceDTO;
import org.example.bookingsystemapp.model.PatientCreateDTO;
import org.example.bookingsystemapp.model.PatientDTO;
import org.example.bookingsystemapp.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientRestController implements PatientApiDelegate {
    private final PatientService patientService;
    private static final String GROUPDID = "${confirmation_boot.groupId}";
    private static final Logger logger = LoggerFactory.getLogger(PatientRestController.class);

    @Override
    @PermitAll
    public ResponseEntity<PatientDTO> createPatient(PatientCreateDTO patientCreateDTO) {
        logger.info("Creating patient: {}", patientCreateDTO);
        PatientDTO patient = patientService.createPatient(patientCreateDTO);
        return ResponseEntity.ok(patient);
    }

    @Override
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PatientDTO> getPatientById(Long patientId) {
        PatientDTO patientDTO = patientService.getPatientById(patientId);
        return ResponseEntity.ok(patientDTO);
    }

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientDTO> updatePatient(PatientDTO patientDTO) {
        patientService.updatePatient(patientDTO);
        return ResponseEntity.ok(patientDTO);
    }

    @Override
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> deletePatient(DeleteReferenceDTO deleteReferenceDTO) {
        patientService.deletePatient(deleteReferenceDTO);
        return ResponseEntity.noContent().build();
    }


}
