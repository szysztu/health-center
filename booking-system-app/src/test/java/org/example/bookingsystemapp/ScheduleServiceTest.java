package org.example.bookingsystemapp;

import org.assertj.core.api.Assertions;
import org.example.bookingsystemapp.entities.Doctor;
import org.example.bookingsystemapp.entities.DoctorSchedule;
import org.example.bookingsystemapp.entities.Patient;
import org.example.bookingsystemapp.exception.*;
import org.example.bookingsystemapp.model.*;
import org.example.bookingsystemapp.repositories.DoctorRepository;
import org.example.bookingsystemapp.repositories.PatientRepository;
import org.example.bookingsystemapp.repositories.ScheduleRepository;
import org.example.bookingsystemapp.service.KafkaProducerService;
import org.example.bookingsystemapp.service.SchedulesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.example.bookingsystemapp.entities.Specialisation.CARDIOLOGIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@ActiveProfiles("test")
@SpringBootTest
public class ScheduleServiceTest {

    @Autowired
    private SchedulesService schedulesService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @Autowired
    TestEntityFactory testEntityFactory;

    TestDtoFactory testDtoFactory = new TestDtoFactory();

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @BeforeEach
    void cleanUp() {
        scheduleRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
    }

    @Test
    void testAddDoctorSchedule() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorScheduleDTO scheduleDTO = testDtoFactory.createDoctorScheduleDTO(doctor.getId());
        schedulesService.addDoctorSchedule(scheduleDTO);
        assertThat(scheduleRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void testAddDoctorScheduleDoctorDoesntExist() {
        DoctorScheduleDTO scheduleDTO = testDtoFactory.createDoctorScheduleDTO(367L);
        Throwable exception = catchThrowable(() -> schedulesService.addDoctorSchedule(scheduleDTO));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doctor not found with id " + 367L);
    }

    @Test
    void testAddDoctorScheduleStartTimeNotValid() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorScheduleDTO scheduleDTO = testDtoFactory.createDoctorScheduleDTO(doctor.getId());
        scheduleDTO.getTermins().get(0).setStartTime(LocalTime.of(14, 47));
        Throwable exception = catchThrowable(() -> schedulesService.addDoctorSchedule(scheduleDTO));
        assertThat(exception)
                .isInstanceOf(InvalidScheduleTime.class)
                .hasMessage("Schedule must start every 30 minutes, for example: 10:00, 10:30, 11:00");
    }

    @Test
    void testAddDoctorScheduleScheduleAlreadyExist() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        DoctorScheduleDTO scheduleDTO2 = testDtoFactory.doctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        Throwable exception = catchThrowable(() -> schedulesService.addDoctorSchedule(scheduleDTO2));
        assertThat(exception)
                .isInstanceOf(NotAvailableException.class)
                .hasMessage("Doctor already has a schedule at " + scheduleDTO2.getTermins().get(0).getStartTime() + " on " + scheduleDTO2.getTermins().get(0).getDay());
    }

    @Test
    void testGetScheduleById() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        DoctorScheduleSingleDTO foundSchedule = schedulesService.getScheduleById(schedule.getId());
        assertThat(foundSchedule)
                .isNotNull()
                .satisfies(s -> {
                    assertThat(s.getDay()).isEqualTo(LocalDate.of(2025, 7, 17));
                    assertThat(s.getStartTime()).isEqualTo(LocalTime.of(14, 0));
                    assertThat(s.getBooked()).isFalse();
                });
    }

    @Test
    void testGetScheduleByIdNegative() {
        Throwable exception = catchThrowable(() -> schedulesService.getScheduleById(45L));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Schedule not found with id " + 45L);
    }

    @Test
    void testUpdateSchedule() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        DoctorScheduleSingleDTO foundSchedule = schedulesService.getScheduleById(schedule.getId());
        foundSchedule.setStartTime(LocalTime.of(11, 0));
        DoctorScheduleSingleDTO updatedSchedule = schedulesService.updateSingleSchedule(foundSchedule);
        assertThat(updatedSchedule)
                .isNotNull()
                .satisfies(s -> {
                    assertThat(s.getDay()).isEqualTo(LocalDate.of(2025, 7, 17));
                    assertThat(s.getStartTime()).isEqualTo(LocalTime.of(11, 0));
                    assertThat(s.getBooked()).isFalse();
                });
    }

    @Test
    void testUpdateScheduleAlreadyExist() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule1 = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        DoctorSchedule schedule2 = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        DoctorScheduleSingleDTO foundSchedule = schedulesService.getScheduleById(schedule2.getId());
        foundSchedule.setStartTime(schedule1.getStartTime());
        Throwable exception = catchThrowable(() -> schedulesService.updateSingleSchedule(foundSchedule));
        assertThat(exception)
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("Doctor already has a schedule at " + foundSchedule.getStartTime() + " on " + foundSchedule.getDay());
    }

    @Test
    void testUpdateScheduleWrongStartTime() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 25), false);
        DoctorScheduleSingleDTO foundSchedule = schedulesService.getScheduleById(schedule.getId());
        foundSchedule.setStartTime(LocalTime.of(15, 25));
        Throwable exception = catchThrowable(() -> schedulesService.updateSingleSchedule(foundSchedule));
        assertThat(exception)
                .isInstanceOf(InvalidScheduleTime.class)
                .hasMessage("Schedule must start every 30 minutes, for example: 10:00, 10:30, 11:00");
    }

    @Test
    void testDeleteSchedule() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 25), false);
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(schedule.getId());
        toDeleteDTO.setVersion(schedule.getVersion());
        schedulesService.deleteSchedule(toDeleteDTO);
        Assertions.assertThat(scheduleRepository.existsById(schedule.getId())).isFalse();
    }

    @Test
    void testDeleteScheduleWrongId() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 25), false);
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(987L);
        toDeleteDTO.setVersion(schedule.getVersion());
        Throwable exception = catchThrowable(() -> schedulesService.deleteSchedule(toDeleteDTO));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Schedule not found with id " + 987L);
    }

    @Test
    void testDeleteScheduleWrongVersion() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 25), false);
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(schedule.getId());
        toDeleteDTO.setVersion(985);
        Throwable exception = catchThrowable(() -> schedulesService.deleteSchedule(toDeleteDTO));
        assertThat(exception)
                .isInstanceOf(VersionMismatchException.class)
                .hasMessage("Version mismatch for schedule with id " + schedule.getId());
    }

    @Test
    void testCreateBooking() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        Patient patient = testEntityFactory.createTestPatient();
        DoctorSchedule schedule = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        doNothing().when(kafkaProducerService).getScheduleData(any(DoctorSchedule.class), any(Patient.class));
        CreateBookingDTO newBookingDTO = new CreateBookingDTO();
        newBookingDTO.setScheduleId(schedule.getId());
        newBookingDTO.setPatientId(patient.getId());
        BookingDTO booked = schedulesService.createBooking(newBookingDTO);
        assertThat(booked)
                .isNotNull()
                .satisfies(s -> {
                    assertThat(s.getDay()).isEqualTo(LocalDate.of(2025, 7, 17));
                    assertThat(s.getStartTime()).isEqualTo(LocalTime.of(14, 0));
                    assertThat(s.getBooked()).isTrue();
                    assertThat(s.getPatientId()).isEqualTo(patient.getId());
                    assertThat(s.getId()).isEqualTo(schedule.getId());
                });
    }

    @Test
    void testCreateBookingScheduleAlreadyTaken() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        Patient patient = testEntityFactory.createTestPatient();
        DoctorSchedule schedule = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), true);
        CreateBookingDTO newBookingDTO = new CreateBookingDTO();
        newBookingDTO.setScheduleId(schedule.getId());
        newBookingDTO.setPatientId(patient.getId());
        Throwable exception = catchThrowable(() -> schedulesService.createBooking(newBookingDTO));
        assertThat(exception)
                .isInstanceOf(NotAvailableException.class)
                .hasMessage("Termin on " + schedule.getDay() + " at " + schedule.getStartTime() + " is already taken");
    }

    @Test
    void testGetFreeScheduleOfDoctor() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule1 = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), true);
        DoctorSchedule schedule2 = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 30), false);
        DoctorSchedule schedule3 = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 30), false);
        List<FreeSchedulesOfDoctorDTO> list = schedulesService.getFreeSchedulesOfDoctor(doctor.getId());
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    void testGetFreeScheduleOfDoctorWrongDoctor() {
        Throwable exception = catchThrowable(() -> schedulesService.getFreeSchedulesOfDoctor(63L));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doctor not found with id " + 63L);
    }

    @Test
    void testGetFreeScheduleByCriteria() {
        Doctor doctor = testEntityFactory.createTestDoctor();
        DoctorSchedule schedule1 = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 16), LocalTime.of(14, 0), true);
        DoctorSchedule schedule2 = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(16, 30), false);
        DoctorSchedule schedule3 = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(17, 30), false);
        DoctorSchedule schedule4 = testEntityFactory.createSchedule(doctor, LocalDate.of(2025, 7, 18), LocalTime.of(14, 30), false);
        ScheduleCriteriaReqDTO criteria = new ScheduleCriteriaReqDTO();
        criteria.setSpecialisation(String.valueOf((CARDIOLOGIST)));
        criteria.setStartDay(LocalDate.of(2025, 7, 16));
        criteria.setEndDay(LocalDate.of(2025, 7, 17));
        criteria.setStartTime(LocalTime.of(16, 0));
        criteria.setEndTime(LocalTime.of(18, 0));
        List<ScheduleCriteriaReturnDTO> list = schedulesService.getSchedulesByCriteria(criteria);
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    void testGetFreeScheduleByCriteriaBadTimeCriteria() {
        ScheduleCriteriaReqDTO criteria = new ScheduleCriteriaReqDTO();
        criteria.setSpecialisation(String.valueOf((CARDIOLOGIST)));
        criteria.setStartDay(LocalDate.of(2025, 7, 22));
        criteria.setEndDay(LocalDate.of(2025, 7, 23));
        criteria.setStartTime(LocalTime.of(16, 0));
        criteria.setEndTime(LocalTime.of(15, 0));
        Throwable exception = catchThrowable(() -> schedulesService.getSchedulesByCriteria(criteria));
        assertThat(exception)
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("Start time cannot be later than end time");
    }

    @Test
    void testGetFreeScheduleByCriteriaBadDayCriteria() {
        ScheduleCriteriaReqDTO criteria = new ScheduleCriteriaReqDTO();
        criteria.setSpecialisation(String.valueOf((CARDIOLOGIST)));
        criteria.setStartDay(LocalDate.of(2025, 8, 19));
        criteria.setEndDay(LocalDate.of(2025, 8, 17));
        criteria.setStartTime(LocalTime.of(11, 0));
        criteria.setEndTime(LocalTime.of(15, 0));
        Throwable exception = catchThrowable(() -> schedulesService.getSchedulesByCriteria(criteria));
        assertThat(exception)
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("Start date cannot be later than end date");
    }
}

