package org.example.confirmationboot.notification;

import lombok.RequiredArgsConstructor;
import org.example.confirmationboot.model.BookingConfirmationClientDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SmsNotificationStrategy implements NotificationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(NotificationStrategy.class);

    @Override
    public void sendNotification(BookingConfirmationClientDTO dto) {
        logger.info("Strategy is working");
    }
}
