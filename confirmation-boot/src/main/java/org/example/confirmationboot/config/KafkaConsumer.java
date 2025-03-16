package org.example.confirmationboot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.confirmationboot.model.BookingConfirmationClientDTO;
import org.example.confirmationboot.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final NotificationService notificationService;
    private static final String TOPIC = "${confirmation_boot.topic}";
    private static final String GROUPDID = "${confirmation_boot.groupId}";
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = TOPIC, groupId = GROUPDID, containerFactory = "kafkaListenerContainerFactory")
    public void consume(BookingConfirmationClientDTO bookingConfirmationClientDTO) {
        logger.info("Message from kafka: " + bookingConfirmationClientDTO);


        notificationService.selectNotification(bookingConfirmationClientDTO);

    }

}

