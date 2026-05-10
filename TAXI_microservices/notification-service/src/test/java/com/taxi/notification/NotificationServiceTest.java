package com.taxi.notification;

import com.taxi.notification.dto.NotificationRequestDto;
import com.taxi.notification.dto.NotificationResponseDto;
import com.taxi.notification.model.NotificationStatus;
import com.taxi.notification.model.NotificationTask;
import com.taxi.notification.repository.NotificationRepository;
import com.taxi.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void maxRetries() {
        ReflectionTestUtils.setField(notificationService, "maxRetries", 3);
    }

    @Test
    void createNotificationPersistsPendingAndReturnsDto() {
        NotificationRequestDto req = new NotificationRequestDto(9L, "DRIVER", 4L, "Hello");
        NotificationTask saved = new NotificationTask();
        saved.setId(100L);
        saved.setTripId(9L);
        saved.setRecipientType("DRIVER");
        saved.setRecipientId(4L);
        saved.setMessage("Hello");
        saved.setStatus(NotificationStatus.PENDING);
        saved.setAttempts(0);
        saved.setCreatedAt(LocalDateTime.now());
        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(NotificationTask.class))).thenReturn(saved);

        NotificationResponseDto dto = notificationService.createNotification(req);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStatus()).isEqualTo("PENDING");
        verify(notificationRepository).save(org.mockito.ArgumentMatchers.argThat(t ->
                t.getTripId().equals(9L)
                        && t.getStatus() == NotificationStatus.PENDING
                        && t.getAttempts() == 0));
    }

    @Test
    void getNotificationsByTripReturnsEmptyWhenNone() {
        when(notificationRepository.findByTripIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        assertThat(notificationService.getNotificationsByTrip(1L)).isEmpty();
    }

    @Test
    void getNotificationsByTripMapsTasks() {
        NotificationTask t = new NotificationTask();
        t.setId(1L);
        t.setTripId(2L);
        t.setRecipientType("PASSENGER");
        t.setRecipientId(3L);
        t.setMessage("m");
        t.setStatus(NotificationStatus.SENT);
        t.setAttempts(1);
        t.setCreatedAt(LocalDateTime.now());
        when(notificationRepository.findByTripIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(t));

        List<NotificationResponseDto> list = notificationService.getNotificationsByTrip(2L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getStatus()).isEqualTo("SENT");
    }

    @Test
    void getQueueStatsAggregatesFourStatuses() {
        when(notificationRepository.countByStatus(NotificationStatus.PENDING)).thenReturn(2L);
        when(notificationRepository.countByStatus(NotificationStatus.PROCESSING)).thenReturn(0L);
        when(notificationRepository.countByStatus(NotificationStatus.SENT)).thenReturn(10L);
        when(notificationRepository.countByStatus(NotificationStatus.FAILED)).thenReturn(1L);

        Map<String, Long> stats = notificationService.getQueueStats();

        assertThat(stats).hasSize(4)
                .containsEntry("PENDING", 2L)
                .containsEntry("PROCESSING", 0L)
                .containsEntry("SENT", 10L)
                .containsEntry("FAILED", 1L);
    }
}
