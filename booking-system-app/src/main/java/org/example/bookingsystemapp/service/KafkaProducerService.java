package org.example.bookingsystemapp.service;


import lombok.AllArgsConstructor;
import org.example.bookingsystemapp.entities.DoctorSchedule;
import org.example.bookingsystemapp.entities.Patient;
import org.example.bookingsystemapp.model.BookingConfirmationDTO;
import org.example.bookingsystemapp.repositories.ScheduleRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, BookingConfirmationDTO> kafkaTemplate;
    private final ScheduleRepository scheduleRepository;

    public void sendMessage(BookingConfirmationDTO bookingConfirmationDTO) {
        kafkaTemplate.send("booking", bookingConfirmationDTO);
    }

    public void getScheduleData(DoctorSchedule doctorSchedule, Patient patient) {
        var bookingConfirmationDTO = new BookingConfirmationDTO();

        bookingConfirmationDTO.setDoctorName(scheduleRepository.findDoctorLastName(doctorSchedule.getId()));
        bookingConfirmationDTO.setPatientEmail(patient.getEmail());
        bookingConfirmationDTO.setPhoneNumber(patient.getPhoneNumber());
        bookingConfirmationDTO.setConfirmationMethod(String.valueOf(patient.getConfirmationMethod()));
        bookingConfirmationDTO.setScheduleDay(doctorSchedule.getDay());
        bookingConfirmationDTO.setScheduleHour(doctorSchedule.getStartTime());

        sendMessage(bookingConfirmationDTO);
    }
}
