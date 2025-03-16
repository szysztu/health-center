package org.example.bookingsystemapp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.assertj.core.api.Assertions;
import org.example.bookingsystemapp.entities.Doctor;
import org.example.bookingsystemapp.entities.Specialisation;
import org.example.bookingsystemapp.exception.InvalidDoctorTokenException;
import org.example.bookingsystemapp.exception.MissingSpecialisationException;
import org.example.bookingsystemapp.exception.NotFoundException;
import org.example.bookingsystemapp.exception.VersionMismatchException;
import org.example.bookingsystemapp.kafka.KafkaProducerConfig;
import org.example.bookingsystemapp.kafka.KafkaTopicConfig;
import org.example.bookingsystemapp.model.DeleteReferenceDTO;
import org.example.bookingsystemapp.model.DoctorCreateDTO;
import org.example.bookingsystemapp.model.DoctorDTO;
import org.example.bookingsystemapp.repositories.DoctorRepository;
import org.example.bookingsystemapp.service.DoctorService;
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
import static org.example.bookingsystemapp.entities.Specialisation.CARDIOLOGIST;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest(classes = BookingSystemAppApplication.class)
@Transactional
public class DoctorServiceTest {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorRepository doctorRepository;

    @PersistenceContext
    private EntityManager entityManager;


    private Doctor doctor;
    private DoctorDTO doctorDTO;
    private DoctorCreateDTO doctorCreateDTO;

    private Doctor createDoctor(String firstName, String lastName, String email, String phone, LocalDate birthDate, Specialisation specialisation) {
        return doctorRepository.save(Doctor.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phone)
                .birthDate(birthDate)
                .specialisation(specialisation)
                .build());
    }

    private DoctorCreateDTO createDoctorDTO(String firstName, String lastName, String email, String phone, LocalDate birthDate, Specialisation specialisation) {
        DoctorCreateDTO dto = new DoctorCreateDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhoneNumber(phone);
        dto.setBirthDate(birthDate);
        dto.setSpecialisation(specialisation != null ? specialisation.toString() : null);
        dto.setToken("DOC");
        return dto;
    }

    private DoctorDTO createDoctorDTO(Long id, String firstName, String lastName, String email, String phone, LocalDate birthDate, Specialisation specialisation, Integer version) {
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

    @BeforeEach
    void setUp() {
        doctor = createDoctor("Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), CARDIOLOGIST);
        doctorDTO = createDoctorDTO(1L, "Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25),  CARDIOLOGIST, 0);
        doctorCreateDTO = createDoctorDTO("Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), CARDIOLOGIST);
    }

    @Test
    public void testOptimisticLocking() {
        Doctor doctorOne = doctorRepository.findById(doctor.getId()).orElseThrow();
        Doctor doctorTwo = doctorRepository.findById(doctor.getId()).orElseThrow();
        entityManager.detach(doctorTwo);

        doctorOne.setFirstName("Updated Name");
        entityManager.flush();
        doctorRepository.save(doctorOne);

        doctorTwo.setFirstName("Updated Name 2");
        entityManager.flush();

        assertThrows(
                ObjectOptimisticLockingFailureException.class, () -> {
                    doctorRepository.save(doctorTwo);
                });
    }

    @Test
    void testFindDoctor() {
        DoctorDTO foundDoctor = doctorService.getDoctorById(doctor.getId());
        assertThat(foundDoctor)
                .isNotNull()
                .satisfies(d -> {
                    assertThat(d.getFirstName()).isEqualTo("Adam");
                    assertThat(d.getLastName()).isEqualTo("Nowak");
                    assertThat(d.getPhoneNumber()).isEqualTo("987654321");
                    assertThat(d.getEmail()).isEqualTo("adam.nowak@example.com");
                    assertThat(d.getSpecialisation()).isEqualTo("CARDIOLOGIST");
                    assertThat(d.getBirthDate()).isEqualTo(LocalDate.of(1985, 3, 25));
                });
    }

    @Test
    void testFindDoctorNegative() {
        Throwable exception = catchThrowable(() -> doctorService.getDoctorById(999L));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doctor not found with id " + 999L);
    }

    @Test
    void testCreateDoctor() {
        DoctorDTO createdDoctor = doctorService.createDoctor(doctorCreateDTO);

        assertThat(createdDoctor)
                .isNotNull()
                .satisfies(d -> {
                    assertThat(d.getFirstName()).isEqualTo("Adam");
                    assertThat(d.getLastName()).isEqualTo("Nowak");
                    assertThat(d.getPhoneNumber()).isEqualTo("987654321");
                    assertThat(d.getEmail()).isEqualTo("adam.nowak@example.com");
                    assertThat(d.getSpecialisation()).isEqualTo("CARDIOLOGIST");
                    assertThat(d.getBirthDate()).isEqualTo(LocalDate.of(1985, 3, 25));
                });
        assertThat(createdDoctor.getId()).isNotNull();
    }

    @Test
    void testCreateDoctorNoSpecialisation() {
        doctorCreateDTO = createDoctorDTO("Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), null);
        Throwable exception = catchThrowable(() -> doctorService.createDoctor(doctorCreateDTO));
        assertThat(exception)
                .isInstanceOf(MissingSpecialisationException.class)
                .hasMessage("Doctor must have specialisation");
    }

    @Test
    void testCreateDoctorWrongToken() {
        doctorCreateDTO = createDoctorDTO("Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), null);
        doctorCreateDTO.setToken("XXX");
        Throwable exception = catchThrowable(() -> doctorService.createDoctor(doctorCreateDTO));
        assertThat(exception)
                .isInstanceOf(InvalidDoctorTokenException.class)
                .hasMessage("Wrong token");

    }

    @Test
    void testUpdateDoctorIdNegative() {
        DoctorDTO doctorDTO = createDoctorDTO(8L, "Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), CARDIOLOGIST, doctor.getVersion());
        Throwable exception = catchThrowable(() -> doctorService.updateDoctor(doctorDTO));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doctor not found with id " + doctorDTO.getId());
    }

    @Test
    void testUpdateDoctorVersionNegative() {
        DoctorDTO doctorDTO = createDoctorDTO(doctor.getId(), "Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), CARDIOLOGIST, 98);
        Throwable exception = catchThrowable(() -> doctorService.updateDoctor(doctorDTO));
        assertThat(exception)
                .isInstanceOf(VersionMismatchException.class)
                .hasMessage("Version mismatch for user with id " + doctor.getId());
    }

    @Test
    void testDeleteDoctorPositive() {
        Doctor doctor1 = createDoctor("Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), CARDIOLOGIST);
        doctorRepository.save(doctor1);
        Long doctorId = doctor1.getId();
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(doctorId);
        toDeleteDTO.setVersion(0);
        doctorService.deleteDoctor(toDeleteDTO);
        Assertions.assertThat(doctorRepository.existsById(doctorId)).isFalse();
    }

    @Test
    void testDeleteDoctorIdNegative() {
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(5L);
        toDeleteDTO.setVersion(doctor.getVersion());
        Throwable exception = Assertions.catchThrowable(() -> doctorService.deleteDoctor(toDeleteDTO));
        Assertions.assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doctor not found with id " + 5L);
    }

    @Test
    void testDeleteDoctorVersionNegative() {
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(doctor.getId());
        toDeleteDTO.setVersion(44);
        Throwable exception = Assertions.catchThrowable(() -> doctorService.deleteDoctor(toDeleteDTO));
        Assertions.assertThat(exception)
                .isInstanceOf(VersionMismatchException.class)
                .hasMessage("Version mismatch for doctor with id " + toDeleteDTO.getId());
    }
}
