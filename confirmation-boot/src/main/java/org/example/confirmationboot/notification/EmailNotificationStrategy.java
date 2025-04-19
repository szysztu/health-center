package org.example.confirmationboot.notification;

import lombok.RequiredArgsConstructor;
import org.example.confirmationboot.model.BookingConfirmationClientDTO;
import org.example.confirmationboot.service.EmailService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EmailNotificationStrategy implements NotificationStrategy {

    private final EmailService emailService;

    @Override
    public void sendNotification(BookingConfirmationClientDTO dto) {
        emailService.sendEmail(dto);
    }
}
