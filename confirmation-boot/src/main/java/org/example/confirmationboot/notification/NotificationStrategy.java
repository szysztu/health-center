package org.example.confirmationboot.notification;

import org.example.confirmationboot.model.BookingConfirmationClientDTO;

public interface NotificationStrategy {
    void sendNotification(BookingConfirmationClientDTO dto);
}
