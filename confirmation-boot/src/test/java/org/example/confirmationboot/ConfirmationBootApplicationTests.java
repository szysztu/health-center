package org.example.confirmationboot;


import org.example.confirmationboot.config.KafkaConsumer;
import org.example.confirmationboot.exception.InvalidConfirmationMethodException;
import org.example.confirmationboot.model.BookingConfirmationClientDTO;
import org.example.confirmationboot.notification.EmailNotificationStrategy;
import org.example.confirmationboot.notification.SmsNotificationStrategy;
import org.example.confirmationboot.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = ConfirmationBootApplication.class)
class ConfirmationBootApplicationTests {

    @Mock
    private EmailNotificationStrategy emailNotificationStrategy;

    @Mock
    private SmsNotificationStrategy smsNotificationStrategy;

    @InjectMocks
    private NotificationService notificationService;

    @MockitoBean
    private KafkaConsumer kafkaConsumer;

    @Test
    void testEmailNotificationStrategy() {
        BookingConfirmationClientDTO confirmationDTO1 = new BookingConfirmationClientDTO();
        confirmationDTO1.setConfirmationMethod("EMAIL");
        confirmationDTO1.setPatientEmail("szymon0szymon@gmail.com");
        confirmationDTO1.setDoctorName("Kowalski");
        confirmationDTO1.setScheduleDay(LocalDate.of(2025, 7, 17));
        confirmationDTO1.setScheduleHour(LocalTime.of(12, 0, 0));
        notificationService.selectNotification(confirmationDTO1);
        verify(emailNotificationStrategy, times(1)).sendNotification(confirmationDTO1);
        verifyNoInteractions(smsNotificationStrategy);
    }

    @Test
    void testSmsNotificationStrategy() {
        BookingConfirmationClientDTO confirmationDTO1 = new BookingConfirmationClientDTO();
        confirmationDTO1.setConfirmationMethod("SMS");
        confirmationDTO1.setPatientEmail("szymon0szymon@gmail.com");
        confirmationDTO1.setDoctorName("Kowalski");
        confirmationDTO1.setScheduleDay(LocalDate.of(2025, 7, 17));
        confirmationDTO1.setScheduleHour(LocalTime.of(12, 0, 0));
        notificationService.selectNotification(confirmationDTO1);
        verify(smsNotificationStrategy, times(1)).sendNotification(confirmationDTO1);
        verifyNoInteractions(emailNotificationStrategy);
    }

    @Test
    void testNotificationStrategyWrongConfirmationMethod() {
        BookingConfirmationClientDTO confirmationDTO1 = new BookingConfirmationClientDTO();
        confirmationDTO1.setConfirmationMethod("XXX");
        confirmationDTO1.setPatientEmail("szymon0szymon@gmail.com");
        confirmationDTO1.setDoctorName("Kowalski");
        confirmationDTO1.setScheduleDay(LocalDate.of(2025, 7, 17));
        confirmationDTO1.setScheduleHour(LocalTime.of(12, 0, 0));
        Throwable exception = catchThrowable(() -> notificationService.selectNotification(confirmationDTO1));
        assertThat(exception)
                .isInstanceOf(InvalidConfirmationMethodException.class)
                .hasMessage("Invalid confirmation method: " + "XXX");
    }
}
