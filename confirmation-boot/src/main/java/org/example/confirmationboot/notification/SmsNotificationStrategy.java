package org.example.confirmationboot.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.confirmationboot.model.BookingConfirmationClientDTO;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class SmsNotificationStrategy implements NotificationStrategy {


    @Override
    public void sendNotification(BookingConfirmationClientDTO dto) {
        log.info("Strategy is working");
    }
}
