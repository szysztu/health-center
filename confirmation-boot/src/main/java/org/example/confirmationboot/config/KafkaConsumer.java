package org.example.confirmationboot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.confirmationboot.model.BookingConfirmationClientDTO;
import org.example.confirmationboot.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final NotificationService notificationService;
    private static final String TOPIC = "${confirmation_boot.topic}";
    private static final String GROUPID = "${confirmation_boot.groupId}";


    @KafkaListener(topics = TOPIC, groupId = GROUPID, containerFactory = "kafkaListenerContainerFactory")
    public void consume(BookingConfirmationClientDTO bookingConfirmationClientDTO) {
        log.info("Message from kafka: " + bookingConfirmationClientDTO);

        notificationService.selectNotification(bookingConfirmationClientDTO);

    }

}

