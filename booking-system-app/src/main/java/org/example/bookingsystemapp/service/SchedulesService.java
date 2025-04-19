package org.example.bookingsystemapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.bookingsystemapp.entities.DoctorSchedule;
import org.example.bookingsystemapp.entities.Specialisation;
import org.example.bookingsystemapp.exception.InvalidDataException;
import org.example.bookingsystemapp.exception.NotAvailableException;
import org.example.bookingsystemapp.exception.NotFoundException;
import org.example.bookingsystemapp.exception.VersionMismatchException;
import org.example.bookingsystemapp.mapper.SchedulesMapper;
import org.example.bookingsystemapp.model.*;
import org.example.bookingsystemapp.repositories.DoctorRepository;
import org.example.bookingsystemapp.repositories.PatientRepository;
import org.example.bookingsystemapp.repositories.ScheduleRepository;
import org.example.bookingsystemapp.service.help.EvictCacheService;
import org.example.bookingsystemapp.service.help.ScheduleValidator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class SchedulesService {

    private final ScheduleRepository scheduleRepository;
    private final SchedulesMapper schedulesMapper;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final KafkaProducerService kafkaProducerService;
    private final EvictCacheService evictCacheService;
    private final ScheduleValidator scheduleValidator;

    public void addDoctorSchedule(DoctorScheduleDTO doctorScheduleDTO) {
        var doctor = doctorRepository.findById(doctorScheduleDTO.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id " + doctorScheduleDTO.getDoctorId()));

        var existingSchedules = scheduleRepository.findFreeSchedulesOfDoctorByDoctorId(doctorScheduleDTO.getDoctorId());

        var schedules = doctorScheduleDTO.getTermins().stream()
                .map(dto -> {
                    scheduleValidator.validateStartTimeOfSchedule(dto.getStartTime());
                    var exists = existingSchedules.stream()
                            .anyMatch(schedule -> schedule.getStartTime().equals(dto.getStartTime()) && schedule.getDay().equals(dto.getDay()));
                    if (exists) {
                        throw new NotAvailableException("Doctor already has a schedule at %s on %s".formatted(dto.getStartTime(),dto.getDay()));
                    }

                    var schedule = schedulesMapper.toDoctorSchedule(dto);
                    schedule.setDoctor(doctor);
                    return schedule;
                })
                .collect(Collectors.toList());

        scheduleRepository.saveAll(schedules);
    }

    public DoctorScheduleSingleDTO getScheduleById(Long id) {
        var schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found with id " + id));

        return schedulesMapper.toDoctorScheduleDTO(schedule);
    }

    public DoctorScheduleSingleDTO updateSingleSchedule(DoctorScheduleSingleDTO doctorScheduleSingleDTO) {
        var schedule = scheduleRepository.findById(doctorScheduleSingleDTO.getId())
                .orElseThrow(() -> new NotFoundException("Schedule not found with id " + doctorScheduleSingleDTO.getId()));

        if (!doctorScheduleSingleDTO.getVersion().equals(schedule.getVersion())) {
            throw new VersionMismatchException("Version mismatch for schedule with id " + doctorScheduleSingleDTO.getId());
        }

        if (doctorScheduleSingleDTO.getDoctorId() != null && !doctorScheduleSingleDTO.getDoctorId().equals(schedule.getDoctor().getId())) {
            throw new IllegalArgumentException("Cannot change the assigned doctor for this schedule entry");
        }

        if (schedule.isBooked() && Optional.ofNullable(doctorScheduleSingleDTO.getBooked()).orElse(false)) {
            schedule.setBooked(false);
            schedule.setPatient(null);
        }

        if (doctorScheduleSingleDTO.getStartTime() != null || doctorScheduleSingleDTO.getDay() != null) {

            scheduleValidator.validateStartTimeAndDayOfSchedule(doctorScheduleSingleDTO.getStartTime(), doctorScheduleSingleDTO.getDay(), schedule);

            if (doctorScheduleSingleDTO.getStartTime() != null) {
                schedule.setStartTime(doctorScheduleSingleDTO.getStartTime());
            }
            if (doctorScheduleSingleDTO.getDay() != null) {
                schedule.setDay(doctorScheduleSingleDTO.getDay());
            }
        }
        var updatedSchedule = scheduleRepository.save(schedule);
        evictCacheService.evictFreeSchedules(schedule.getDoctor().getId());

        return schedulesMapper.toDoctorScheduleDTO(updatedSchedule);
    }

    public void deleteSchedule(DeleteReferenceDTO deleteReferenceDTO) {
        var schedule = scheduleRepository.findById(deleteReferenceDTO.getId())
                .orElseThrow(() -> new NotFoundException("Schedule not found with id " + deleteReferenceDTO.getId()));

        if (!deleteReferenceDTO.getVersion().equals(schedule.getVersion())) {
            throw new VersionMismatchException("Version mismatch for schedule with id " + deleteReferenceDTO.getId());
        }
        scheduleRepository.deleteById(deleteReferenceDTO.getId());
    }


    public BookingDTO createBooking(CreateBookingDTO createBookingDTO) {
        var patient = patientRepository.findById(createBookingDTO.getPatientId())
                .orElseThrow(() -> new NotFoundException("User not found with id " + createBookingDTO.getPatientId()));

        var schedule = scheduleRepository.findById(createBookingDTO.getScheduleId())
                .orElseThrow(() -> new NotFoundException("Schedule not found with id " + createBookingDTO.getScheduleId()));

        if (schedule.isBooked()) {
            throw new NotAvailableException("Termin on %s at %s is already taken".formatted(schedule.getDay(), schedule.getStartTime()));
        }
        schedule.setPatient(patient);
        schedule.setBooked(true);

        scheduleRepository.save(schedule);
        evictCacheService.evictFreeSchedules(schedule.getDoctor().getId());
        kafkaProducerService.getScheduleData(schedule, patient);

        return schedulesMapper.toBookingDTO(schedule);
    }

    public List<ScheduleCriteriaReturnDTO> getSchedulesByCriteria(ScheduleCriteriaReqDTO scheduleCriteriaReqDTO) {

        if (scheduleCriteriaReqDTO.getStartDay().isAfter(scheduleCriteriaReqDTO.getEndDay())) {
            throw new InvalidDataException("Start date cannot be later than end date");
        }

        if (scheduleCriteriaReqDTO.getStartTime() != null && scheduleCriteriaReqDTO.getEndTime() != null) {
            if (scheduleCriteriaReqDTO.getStartTime().isAfter(scheduleCriteriaReqDTO.getEndTime())) {
                throw new InvalidDataException("Start time cannot be later than end time");
            }
        }

        var schedules = scheduleRepository.findSchedulesByCriteria(
                scheduleCriteriaReqDTO.getStartDay(),
                scheduleCriteriaReqDTO.getEndDay(),
                scheduleCriteriaReqDTO.getStartTime(),
                scheduleCriteriaReqDTO.getEndTime(),
                Specialisation.valueOf(scheduleCriteriaReqDTO.getSpecialisation())
        );

        return schedules.stream()
                .map(temp -> {
                    ScheduleCriteriaReturnDTO dto = new ScheduleCriteriaReturnDTO();
                    dto.setDoctorId(temp.getDoctorId());
                    dto.setDoctorLastName(temp.getDoctorLastName());
                    if (temp.getSpecialisation() != null) {
                        dto.setSpecialisation(String.valueOf(temp.getSpecialisation()));
                    }
                    dto.setDay(temp.getDay());
                    dto.setStartTime(temp.getStartTime());
                    dto.setBooked(temp.isBooked());
                    return dto;
                })
                .toList();
    }

    @Cacheable(value = "freeSchedules", key = "#doctorId")
    public List<FreeSchedulesOfDoctorDTO> getFreeSchedulesOfDoctor(Long doctorId) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id " + doctorId));

        var doctorSchedulesList = scheduleRepository.findFreeSchedulesOfDoctorByDoctorId(doctor.getId());

        return doctorSchedulesList.stream()
                .map(temp -> {
                    FreeSchedulesOfDoctorDTO dto = new FreeSchedulesOfDoctorDTO();
                    dto.setDoctorId(doctorId);
                    dto.setDay(temp.getDay());
                    dto.setStartTime(temp.getStartTime());
                    dto.setScheduleId(temp.getId());
                    return dto;
                }).toList();
    }

}
