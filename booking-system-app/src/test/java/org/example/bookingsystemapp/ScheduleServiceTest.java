package org.example.bookingsystemapp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.assertj.core.api.Assertions;
import org.example.bookingsystemapp.entities.*;
import org.example.bookingsystemapp.exception.*;
import org.example.bookingsystemapp.kafka.KafkaTopicConfig;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.example.bookingsystemapp.entities.ConfirmationMethod.EMAIL;
import static org.example.bookingsystemapp.entities.Specialisation.CARDIOLOGIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@ActiveProfiles("test")
@SpringBootTest(classes = BookingSystemAppApplication.class)
@Transactional
public class ScheduleServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SchedulesService schedulesService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    private Doctor doctor;
    private Patient patient;
    private DoctorScheduleSingleDTO scheduleSingleDTO;
    private DoctorScheduleDTO scheduleDTO;
    private DoctorScheduleDTO scheduleDTO1;
    private DoctorScheduleDTO scheduleDTO2;
    private DoctorScheduleDTO scheduleDTO3;
    private DoctorScheduleDTO scheduleDTO4;

    @BeforeEach
    void setUp() {
        doctor = createDoctor("Adam", "Nowak", "adam.nowak@example.com", "987654321", LocalDate.of(1985, 3, 25), CARDIOLOGIST);
        patient = createPatient("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", LocalDate.of(1995, 7, 17), EMAIL);
        scheduleDTO = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
    }

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

    private DoctorSchedule createSchedule(Doctor doctor, LocalDate day, LocalTime startTime, boolean booked) {
        return scheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .day(day)
                .startTime(startTime)
                .booked(booked)
                .build());
    }

    private DoctorScheduleDTO createDoctorScheduleDTO(Long doctorId, LocalDate day, LocalTime startTime, boolean booked) {
        SingleTerminDTO termin = new SingleTerminDTO();
        termin.setDay(day);
        termin.setStartTime(startTime);
        termin.setBooked(booked);

        DoctorScheduleDTO dto = new DoctorScheduleDTO();
        dto.setDoctorId(doctorId);
        dto.setTermins(Collections.singletonList(termin));
        return dto;
    }

    @Test
    void testAddDoctorSchedule() {
        schedulesService.addDoctorSchedule(scheduleDTO);
        assertThat(scheduleRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void testAddDoctorScheduleDoctorDoesntExist() {
        scheduleDTO.setDoctorId(87L);

        Throwable exception = catchThrowable(() -> schedulesService.addDoctorSchedule(scheduleDTO));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doctor not found with id " + 87L);
    }

    @Test
    void testAddDoctorScheduleStartTimeNotValid() {
        scheduleDTO = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(14, 45), false);

        Throwable exception = catchThrowable(() -> schedulesService.addDoctorSchedule(scheduleDTO));
        assertThat(exception)
                .isInstanceOf(InvalidScheduleTime.class)
                .hasMessage("Schedule must start every 30 minutes, for example: 10:00, 10:30, 11:00");
    }

    @Test
    void testAddDoctorScheduleScheduleAlreadyExist() {
        scheduleDTO = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(14, 00), false);
        schedulesService.addDoctorSchedule(scheduleDTO);
        scheduleDTO1 = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(14, 00), false);

        Throwable exception = catchThrowable(() -> schedulesService.addDoctorSchedule(scheduleDTO1));
        assertThat(exception)
                .isInstanceOf(NotAvailableException.class)
                .hasMessage("Doctor already has a schedule at " + scheduleDTO1.getTermins().get(0).getStartTime() + " on " + scheduleDTO1.getTermins().get(0).getDay());
    }

    @Test
    void testGetScheduleById() {
        DoctorSchedule schedule = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
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
        DoctorSchedule schedule = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
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
        DoctorSchedule schedule = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        DoctorSchedule schedule2 = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(15, 0), false);

        DoctorScheduleSingleDTO foundSchedule = schedulesService.getScheduleById(schedule.getId());
        foundSchedule.setStartTime(LocalTime.of(15, 0));

        Throwable exception = catchThrowable(() -> schedulesService.updateSingleSchedule(foundSchedule));
        assertThat(exception)
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("Doctor already has a schedule at " + foundSchedule.getStartTime() + " on " + foundSchedule.getDay());
    }

    @Test
    void testUpdateScheduleWrongStartTime() {
        DoctorSchedule schedule = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);

        DoctorScheduleSingleDTO foundSchedule = schedulesService.getScheduleById(schedule.getId());
        foundSchedule.setStartTime(LocalTime.of(15, 25));

        Throwable exception = catchThrowable(() -> schedulesService.updateSingleSchedule(foundSchedule));
        assertThat(exception)
                .isInstanceOf(InvalidScheduleTime.class)
                .hasMessage("Schedule must start every 30 minutes, for example: 10:00, 10:30, 11:00");
    }

    @Test
    void testDeleteSchedule() {
        DoctorSchedule schedule = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        DeleteReferenceDTO toDeleteDTO = new DeleteReferenceDTO();
        toDeleteDTO.setId(schedule.getId());
        toDeleteDTO.setVersion(schedule.getVersion());

        schedulesService.deleteSchedule(toDeleteDTO);
        Assertions.assertThat(patientRepository.existsById(schedule.getId())).isFalse();
    }

    @Test
    void testDeleteScheduleWrongId() {
        DoctorSchedule schedule = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
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
        DoctorSchedule schedule = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
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
        DoctorSchedule schedule = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
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
        DoctorSchedule schedule = createSchedule(doctor, LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), true);

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
        scheduleDTO1 = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(11, 0), false);
        schedulesService.addDoctorSchedule(scheduleDTO1);
        scheduleDTO2 = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(11, 30), true);
        schedulesService.addDoctorSchedule(scheduleDTO2);
        scheduleDTO3 = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        schedulesService.addDoctorSchedule(scheduleDTO3);

        List<FreeSchedulesOfDoctorDTO> list = schedulesService.getFreeSchedulesOfDoctor(doctor.getId());

        assertThat(list.size()).isEqualTo(2);

    }

    @Test
    void testGetFreeScheduleOfDoctorWrongDoctor() {
        Throwable exception = catchThrowable(() -> schedulesService.getFreeSchedulesOfDoctor(2L));
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doctor not found with id " + 2L);

    }

    @Test
    void testGetFreeScheduleByCriteria() {
        scheduleDTO1 = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 16), LocalTime.of(11, 0), false);
        schedulesService.addDoctorSchedule(scheduleDTO1);
        scheduleDTO2 = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(11, 30), false);
        schedulesService.addDoctorSchedule(scheduleDTO2);
        scheduleDTO3 = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 18), LocalTime.of(14, 0), false);
        schedulesService.addDoctorSchedule(scheduleDTO3);
        scheduleDTO4 = createDoctorScheduleDTO(doctor.getId(), LocalDate.of(2025, 7, 17), LocalTime.of(14, 0), false);
        schedulesService.addDoctorSchedule(scheduleDTO4);

        ScheduleCriteriaReqDTO criteria = new ScheduleCriteriaReqDTO();
        criteria.setSpecialisation(String.valueOf((CARDIOLOGIST)));
        criteria.setStartDay(LocalDate.of(2025, 7, 16));
        criteria.setEndDay(LocalDate.of(2025, 7, 17));
        criteria.setStartTime(LocalTime.of(13, 0));
        criteria.setEndTime(LocalTime.of(15, 0));

        List<ScheduleCriteriaReturnDTO> list = schedulesService.getSchedulesByCriteria(criteria);

        assertThat(list.size()).isEqualTo(1);

    }

    @Test
    void testGetFreeScheduleByCriteriaBadTimeCriteria() {
        ScheduleCriteriaReqDTO criteria = new ScheduleCriteriaReqDTO();
        criteria.setSpecialisation(String.valueOf((CARDIOLOGIST)));
        criteria.setStartDay(LocalDate.of(2025, 7, 16));
        criteria.setEndDay(LocalDate.of(2025, 7, 17));
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
        criteria.setStartDay(LocalDate.of(2025, 7, 19));
        criteria.setEndDay(LocalDate.of(2025, 7, 17));
        criteria.setStartTime(LocalTime.of(11, 0));
        criteria.setEndTime(LocalTime.of(15, 0));

        Throwable exception = catchThrowable(() -> schedulesService.getSchedulesByCriteria(criteria));
        assertThat(exception)
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("Start date cannot be later than end date");
    }

}

