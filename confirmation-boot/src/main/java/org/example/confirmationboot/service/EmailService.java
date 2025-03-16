package org.example.confirmationboot.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.EmailValidator;
import org.example.confirmationboot.config.KafkaConsumer;
import org.example.confirmationboot.model.BookingConfirmationClientDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String EMAIL = "${spring.mail.username}";

    public void sendEmail(BookingConfirmationClientDTO bookingConfirmationClientDTO) {

        var subject = "Confirmation of new schedule booking on " + bookingConfirmationClientDTO.getScheduleDay();
        var messageContent = "Booking was created successfully on "
                + bookingConfirmationClientDTO.getScheduleDay() +
                " at " + bookingConfirmationClientDTO.getScheduleHour() +
                " doctor " + bookingConfirmationClientDTO.getDoctorName();

        try {
            var message = new SimpleMailMessage();
            message.setFrom(EMAIL);
            message.setTo(bookingConfirmationClientDTO.getPatientEmail());
            message.setSubject(subject);
            message.setText(messageContent);
            mailSender.send(message);

            logger.info("Email sent to " + bookingConfirmationClientDTO.getPatientEmail());
        } catch (MailException e) {
            logger.info("Error sending email: " + e.getMessage());
        }
    }
}
