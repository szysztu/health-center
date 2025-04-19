package org.example.bookingsystemapp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.example.bookingsystemapp.entities.Patient;
import org.example.bookingsystemapp.exception.NotFoundException;
import org.example.bookingsystemapp.exception.VersionMismatchException;
import org.example.bookingsystemapp.model.DeleteReferenceDTO;
import org.example.bookingsystemapp.model.PatientCreateDTO;
import org.example.bookingsystemapp.model.PatientDTO;
import org.example.bookingsystemapp.repositories.PatientRepository;
import org.example.bookingsystemapp.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
public class PatientServiceTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    TestEntityFactory testEntityFactory;

    TestDtoFactory testDtoFactory = new TestDtoFactory();

    @Transactional
    @Test
    public void testOptimisticLocking() {
        Patient patient = testEntityFactory.createTestPatient();
        Patient patientOne = patientRepository.findById(patient.getId()).orElseThrow();
        Patient patientTwo = patientRepository.findById(patient.getId()).orElseThrow();
        entityManager.detach(patientTwo);
        patientOne.setFirstName("Updated Name");
        entityManager.flush();
        patientRepository.save(patientOne);
        patientTwo.setFirstName("Updated Name 2");
        entityManager.flush();
        assertThrows(
                ObjectOptimisticLockingFailureException.class, () -> {
                    patientRepository.save(patientTwo);
                });
    }

    @Test
    void testFindPatient() {
        Patient patient = testEntityFactory.createTestPatient();
        PatientDTO foundPatient = patientService.getPatientById(patient.getId());
        assertThat(foundPatient)
                .isNotNull()
                .satisfies(p -> {
                    assertThat(p.getFirstName()).isEqualTo("Jan");
                    assertThat(p.getLastName()).isEqualTo("Kowalski");
                    assertThat(p.getPhoneNumber()).isEqualTo("123456789");
                    assertThat(p.getEmail()).isEqualTo("jan.kowalski@example.com");
                    assertThat(p.getBirthDate()).isEqualTo(LocalDate.of(1995, 7, 17));
                    assertThat(p.getConfirmationMethod()).isEqualTo("SMS");
                });
    }

    @Test
    void testFindPatientNegative() {
        Throwable exception = catchThrowable(() -> patientService.getPatientById(999L));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Patient not found with id " + 999L);
    }

    @Test
    void testCreatePatient() {
        PatientCreateDTO patientCreateDTO = testDtoFactory.createPatientCreateDTO();
        PatientDTO createdPatient = patientService.createPatient(patientCreateDTO);
        assertThat(createdPatient)
                .isNotNull()
                .satisfies(p -> {
                    assertThat(p.getFirstName()).isEqualTo("Jan");
                    assertThat(p.getLastName()).isEqualTo("Kowalski");
                    assertThat(p.getPhoneNumber()).isEqualTo("123456789");
                    assertThat(p.getEmail()).isEqualTo("jan.kowalski@example.com");
                    assertThat(p.getBirthDate()).isEqualTo(LocalDate.of(1995, 7, 17));
                });
        assertThat(createdPatient.getId()).isNotNull();
    }

    @Test
    void testUpdatePatientIdNegative() {
        PatientCreateDTO patientCreateDTO = testDtoFactory.createPatientCreateDTO();
        PatientDTO createdPatient = patientService.createPatient(patientCreateDTO);
        PatientDTO patientDTO = testDtoFactory.createPatientDTO(43L, createdPatient.getVersion());
        Throwable exception = catchThrowable(() -> patientService.updatePatient(patientDTO));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Patient not found with id " + 43L);
    }

    @Test
    void testUpdatePatientVersionNegative() {
        PatientCreateDTO patientCreateDTO = testDtoFactory.createPatientCreateDTO();
        PatientDTO createdPatient = patientService.createPatient(patientCreateDTO);
        PatientDTO patientDTO = testDtoFactory.createPatientDTO(createdPatient.getId(), 23);
        Throwable exception = catchThrowable(() -> patientService.updatePatient(patientDTO));
        assertThat(exception)
                .isInstanceOf(VersionMismatchException.class)
                .hasMessage("Version mismatch for patient with id " + patientDTO.getId());
    }

    @Test
    void testDeletePatientPositive() {
        PatientCreateDTO patientCreateDTO = testDtoFactory.createPatientCreateDTO();
        PatientDTO createdPatient = patientService.createPatient(patientCreateDTO);
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(createdPatient.getId());
        toDeleteDTO.setVersion(createdPatient.getVersion());
        patientService.deletePatient(toDeleteDTO);
        Assertions.assertThat(patientRepository.existsById(createdPatient.getId())).isFalse();
    }

    @Test
    void testDeletePatientIdNegative() {
        PatientCreateDTO patientCreateDTO = testDtoFactory.createPatientCreateDTO();
        PatientDTO createdPatient = patientService.createPatient(patientCreateDTO);
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(5L);
        toDeleteDTO.setVersion(createdPatient.getVersion());
        Throwable exception = Assertions.catchThrowable(() -> patientService.deletePatient(toDeleteDTO));
        Assertions.assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Patient not found with id " + 5L);
    }

    @Test
    void testDeletePatientVersionNegative() {
        PatientCreateDTO patientCreateDTO = testDtoFactory.createPatientCreateDTO();
        PatientDTO createdPatient = patientService.createPatient(patientCreateDTO);
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(createdPatient.getId());
        toDeleteDTO.setVersion(12);
        Throwable exception = Assertions.catchThrowable(() -> patientService.deletePatient(toDeleteDTO));
        Assertions.assertThat(exception)
                .isInstanceOf(VersionMismatchException.class)
                .hasMessage("Version mismatch for patient with id " + toDeleteDTO.getId());
    }
}

