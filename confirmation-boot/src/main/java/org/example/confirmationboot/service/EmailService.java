package org.example.confirmationboot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.confirmationboot.model.BookingConfirmationClientDTO;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private static final String EMAIL = "${spring.mail.username}";

    public void sendEmail(BookingConfirmationClientDTO bookingConfirmationClientDTO) {

        var subject = "Confirmation of new schedule booking on " + bookingConfirmationClientDTO.getScheduleDay();

        var messageContent = "Booking was created successfully on %s at %s doctor %s"
                .formatted(bookingConfirmationClientDTO.getScheduleDay(),
                bookingConfirmationClientDTO.getScheduleHour(),
                bookingConfirmationClientDTO.getDoctorName());

        try {
            var message = new SimpleMailMessage();
            message.setFrom(EMAIL);
            message.setTo(bookingConfirmationClientDTO.getPatientEmail());
            message.setSubject(subject);
            message.setText(messageContent);
            mailSender.send(message);

            log.info("Email sent to " + bookingConfirmationClientDTO.getPatientEmail());
        } catch (MailException e) {
            log.info("Error sending email: " + e.getMessage());
        }
    }
}
