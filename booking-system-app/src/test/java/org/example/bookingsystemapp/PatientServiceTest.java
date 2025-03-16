package org.example.bookingsystemapp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.example.bookingsystemapp.entities.ConfirmationMethod;
import org.example.bookingsystemapp.entities.Patient;
import org.example.bookingsystemapp.exception.NotFoundException;
import org.example.bookingsystemapp.exception.VersionMismatchException;
import org.example.bookingsystemapp.model.DeleteReferenceDTO;
import org.example.bookingsystemapp.model.PatientCreateDTO;
import org.example.bookingsystemapp.model.PatientDTO;
import org.example.bookingsystemapp.repositories.PatientRepository;
import org.example.bookingsystemapp.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.example.bookingsystemapp.entities.ConfirmationMethod.EMAIL;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest(classes = BookingSystemAppApplication.class)
@Transactional
public class PatientServiceTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Patient patient;
    private PatientDTO patientDTO;
    private PatientCreateDTO patientCreateNegativDTO;
    private PatientCreateDTO patientCreateDTO;

    private Patient createPatient(String firstName, String lastName, String email, String phone, LocalDate birthDate, ConfirmationMethod confirmationMethod) {
        return patientRepository.save(Patient.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phone)
                .birthDate(birthDate)
                .confirmationMethod(confirmationMethod)
                .build());
    }

    private PatientCreateDTO createPatientDTO(String firstName, String lastName, String email, String phone, LocalDate birthDate) {
        PatientCreateDTO dto = new PatientCreateDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhoneNumber(phone);
        dto.setBirthDate(birthDate);
        return dto;
    }

    private PatientDTO createPatientDTO(Long id, String firstName, String lastName, String email, String phone, LocalDate birthDate, Integer version) {
        PatientDTO dto = new PatientDTO();
        dto.setId(id);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhoneNumber(phone);
        dto.setBirthDate(birthDate);
        dto.setVersion(version);
        return dto;
    }

    @BeforeEach
    void setUp() {
        patient = createPatient("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1995, 7, 17), EMAIL);
        patientDTO = createPatientDTO(1L, "Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1990, 5, 20), 0);
        patientCreateDTO = createPatientDTO("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1990, 5, 20));
        patientCreateNegativDTO = createPatientDTO("Ryszard", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1990, 5, 20));
    }

    @Test
    public void testOptimisticLocking() {
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
        PatientDTO foundPatient = patientService.getPatientById(patient.getId());
        assertThat(foundPatient)
                .isNotNull()
                .satisfies(p -> {
                    assertThat(p.getFirstName()).isEqualTo("Jan");
                    assertThat(p.getLastName()).isEqualTo("Kowalski");
                    assertThat(p.getPhoneNumber()).isEqualTo("123456789");
                    assertThat(p.getEmail()).isEqualTo("jan.kowalski@example.com");
                    assertThat(p.getBirthDate()).isEqualTo(LocalDate.of(1995, 7, 17));
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
//        when(keycloakService.createUserKeycloak(any(Patient.class))).thenReturn(201);
        PatientDTO createdPatient = patientService.createPatient(patientCreateDTO);

        assertThat(createdPatient)
                .isNotNull()
                .satisfies(p -> {
                    assertThat(p.getFirstName()).isEqualTo("Jan");
                    assertThat(p.getLastName()).isEqualTo("Kowalski");
                    assertThat(p.getPhoneNumber()).isEqualTo("123456789");
                    assertThat(p.getEmail()).isEqualTo("jan.kowalski@example.com");
                    assertThat(p.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 20));
                });
        assertThat(createdPatient.getId()).isNotNull();
    }

    @Test
    void testUpdatePatientIdNegative() {
        PatientDTO patientDTO = createPatientDTO(8L, "Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1990, 5, 20), 0);
        Throwable exception = catchThrowable(() -> patientService.updatePatient(patientDTO));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Patient not found with id " + 8L);
    }

    @Test
    void testUpdatePatientVersionNegative() {
        PatientDTO patientDTO = createPatientDTO(patient.getId(), "Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1990, 5, 20), 98);
        Throwable exception = catchThrowable(() -> patientService.updatePatient(patientDTO));
        assertThat(exception)
                .isInstanceOf(VersionMismatchException.class)
                .hasMessage("Version mismatch for patient with id " + patient.getId());
    }

    @Test
    void testDeletePatientPositive() {
        Patient patient1 = createPatient("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1995, 7, 17), null);
        patientRepository.save(patient1);
        Long patientId = patient1.getId();
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(patientId);
        toDeleteDTO.setVersion(0);
        patientService.deletePatient(toDeleteDTO);
        Assertions.assertThat(patientRepository.existsById(patientId)).isFalse();
    }

    @Test
    void testDeletePatientIdNegative() {
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(5L);
        toDeleteDTO.setVersion(patient.getVersion());
        Throwable exception = Assertions.catchThrowable(() -> patientService.deletePatient(toDeleteDTO));
        Assertions.assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Patient not found with id " + 5L);
    }

    @Test
    void testDeletePatientVersionNegative() {
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(patient.getId());
        toDeleteDTO.setVersion(4);
        Throwable exception = Assertions.catchThrowable(() -> patientService.deletePatient(toDeleteDTO));
        Assertions.assertThat(exception)
                .isInstanceOf(VersionMismatchException.class)
                .hasMessage("Version mismatch for patient with id " + toDeleteDTO.getId());
    }
}

