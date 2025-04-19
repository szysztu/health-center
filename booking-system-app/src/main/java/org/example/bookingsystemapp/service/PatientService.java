package org.example.bookingsystemapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.bookingsystemapp.exception.NotFoundException;
import org.example.bookingsystemapp.exception.VersionMismatchException;
import org.example.bookingsystemapp.mapper.PatientMapper;
import org.example.bookingsystemapp.model.DeleteReferenceDTO;
import org.example.bookingsystemapp.model.PatientCreateDTO;
import org.example.bookingsystemapp.model.PatientDTO;
import org.example.bookingsystemapp.repositories.PatientRepository;
import org.springframework.stereotype.Service;


@Transactional
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientDTO createPatient(PatientCreateDTO patientCreateDTO) {
        var patient = patientMapper.toPatient(patientCreateDTO);

        patientRepository.save(patient);

        return patientMapper.toPatientDTO(patient);
    }

    public PatientDTO getPatientById(Long id) {
        return patientRepository.findById(id).map(patientMapper::toPatientDTO)
                .orElseThrow(() -> new NotFoundException("Patient not found with id " + id));
    }

    public PatientDTO updatePatient(PatientDTO PatientDTO) {
        var patient = patientRepository.findById(PatientDTO.getId())
                .orElseThrow(() -> new NotFoundException("Patient not found with id " + PatientDTO.getId()));
        if (!PatientDTO.getVersion().equals(patient.getVersion())) {
            throw new VersionMismatchException("Version mismatch for patient with id " + PatientDTO.getId());
        }

        if (PatientDTO.getEmail() != null) {
            patient.setEmail(PatientDTO.getEmail());
        }
        if (PatientDTO.getFirstName() != null) {
            patient.setFirstName(PatientDTO.getFirstName());
        }
        if (PatientDTO.getLastName() != null) {
            patient.setLastName(PatientDTO.getLastName());
        }
        if (PatientDTO.getPhoneNumber() != null) {
            patient.setPhoneNumber(PatientDTO.getPhoneNumber());
        }
        if (PatientDTO.getBirthDate() != null) {
            patient.setBirthDate(PatientDTO.getBirthDate());
        }

        var updatedPatient = patientRepository.save(patient);
        return patientMapper.toPatientDTO(updatedPatient);
    }

    public void deletePatient(DeleteReferenceDTO deleteReferenceDTO) {
        var patient = patientRepository.findById(deleteReferenceDTO.getId())
                .orElseThrow(() -> new NotFoundException("Patient not found with id " + deleteReferenceDTO.getId()));
        if (!deleteReferenceDTO.getVersion().equals(patient.getVersion())) {
            throw new VersionMismatchException("Version mismatch for patient with id " + deleteReferenceDTO.getId());
        }
        patientRepository.deleteById(deleteReferenceDTO.getId());
    }

}
