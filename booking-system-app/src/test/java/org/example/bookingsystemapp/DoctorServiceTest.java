package org.example.bookingsystemapp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.example.bookingsystemapp.entities.Doctor;
import org.example.bookingsystemapp.exception.MissingSpecialisationException;
import org.example.bookingsystemapp.exception.NotFoundException;
import org.example.bookingsystemapp.exception.VersionMismatchException;
import org.example.bookingsystemapp.model.DeleteReferenceDTO;
import org.example.bookingsystemapp.model.DoctorCreateDTO;
import org.example.bookingsystemapp.model.DoctorDTO;
import org.example.bookingsystemapp.repositories.DoctorRepository;
import org.example.bookingsystemapp.service.DoctorService;
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
public class DoctorServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    TestEntityFactory testEntityFactory;

    @Autowired
    DoctorService doctorService;

    @Autowired
    private DoctorRepository doctorRepository;

    TestDtoFactory testDtoFactory = new TestDtoFactory();

    @Transactional
    @Test
    public void testOptimisticLocking() {
        Doctor doctor = testEntityFactory.createTestDoctor();
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
        Doctor doctor = testEntityFactory.createTestDoctor();
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
        DoctorCreateDTO doctorCreatedDTO = testDtoFactory.createDoctorCreateDTO();
        DoctorDTO createdDoctor = doctorService.createDoctor(doctorCreatedDTO);
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
        DoctorCreateDTO doctorCreatedDTO = testDtoFactory.createDoctorCreateDTO();
        doctorCreatedDTO.setSpecialisation(null);
        Throwable exception = catchThrowable(() -> doctorService.createDoctor(doctorCreatedDTO));
        assertThat(exception)
                .isInstanceOf(MissingSpecialisationException.class)
                .hasMessage("Doctor must have specialisation");
    }

    @Test
    void testUpdateDoctorIdNegative() {
        DoctorCreateDTO doctorCreatedDTO = testDtoFactory.createDoctorCreateDTO();
        DoctorDTO createdDoctor = doctorService.createDoctor(doctorCreatedDTO);
        DoctorDTO doctorDTO = testDtoFactory.createTestDoctorDTO(8L, createdDoctor.getVersion());
        Throwable exception = catchThrowable(() -> doctorService.updateDoctor(doctorDTO));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doctor not found with id " + doctorDTO.getId());
    }

    @Test
    void testUpdateDoctorVersionNegative() {
        DoctorCreateDTO doctorCreatedDTO = testDtoFactory.createDoctorCreateDTO();
        DoctorDTO createdDoctor = doctorService.createDoctor(doctorCreatedDTO);
        DoctorDTO doctorDTO = testDtoFactory.createTestDoctorDTO(createdDoctor.getId(), 99);
        Throwable exception = catchThrowable(() -> doctorService.updateDoctor(doctorDTO));
        assertThat(exception)
                .isInstanceOf(VersionMismatchException.class)
                .hasMessage("Version mismatch for user with id " + doctorDTO.getId());
    }

    @Test
    void testDeleteDoctorPositive() {
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        DoctorCreateDTO doctorCreatedDTO = testDtoFactory.createDoctorCreateDTO();
        DoctorDTO createdDoctor = doctorService.createDoctor(doctorCreatedDTO);
        toDeleteDTO.setId(createdDoctor.getId());
        toDeleteDTO.setVersion(createdDoctor.getVersion());
        doctorService.deleteDoctor(toDeleteDTO);
        Assertions.assertThat(doctorRepository.existsById(createdDoctor.getId())).isFalse();
    }

    @Test
    void testDeleteDoctorIdNegative() {
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        DoctorCreateDTO doctorCreatedDTO = testDtoFactory.createDoctorCreateDTO();
        DoctorDTO createdDoctor = doctorService.createDoctor(doctorCreatedDTO);
        toDeleteDTO.setId(555L);
        toDeleteDTO.setVersion(createdDoctor.getVersion());
        Throwable exception = Assertions.catchThrowable(() -> doctorService.deleteDoctor(toDeleteDTO));
        Assertions.assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doctor not found with id " + 555L);
    }

    @Test
    void testDeleteDoctorVersionNegative() {
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        DoctorCreateDTO doctorCreatedDTO = testDtoFactory.createDoctorCreateDTO();
        DoctorDTO createdDoctor = doctorService.createDoctor(doctorCreatedDTO);
        toDeleteDTO.setId(createdDoctor.getId());
        toDeleteDTO.setVersion(9);
        Throwable exception = Assertions.catchThrowable(() -> doctorService.deleteDoctor(toDeleteDTO));
        Assertions.assertThat(exception)
                .isInstanceOf(VersionMismatchException.class)
                .hasMessage("Version mismatch for doctor with id " + toDeleteDTO.getId());
    }
}
