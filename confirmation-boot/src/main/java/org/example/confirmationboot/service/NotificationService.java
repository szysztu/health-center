package org.example.confirmationboot.service;

import lombok.RequiredArgsConstructor;
import org.example.confirmationboot.model.BookingConfirmationClientDTO;
import org.example.confirmationboot.model.ConfirmationMethod;
import org.example.confirmationboot.notification.EmailNotificationStrategy;
import org.example.confirmationboot.notification.SmsNotificationStrategy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailNotificationStrategy emailNotificationStrategy;
    private final SmsNotificationStrategy smsNotificationStrategy;

    public void selectNotification(BookingConfirmationClientDTO bookingConfirmationClientDTO) {
        ConfirmationMethod method;
        try {
            method = ConfirmationMethod.valueOf(bookingConfirmationClientDTO.getConfirmationMethod());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid confirmation method: " + bookingConfirmationClientDTO.getConfirmationMethod());
        }

        if (method.equals(ConfirmationMethod.EMAIL)){
            emailNotificationStrategy.sendNotification(bookingConfirmationClientDTO);
        } else if (method.equals(ConfirmationMethod.SMS)){
            smsNotificationStrategy.sendNotification(bookingConfirmationClientDTO);
        }
    }
}
