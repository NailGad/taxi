package com.taxi.notification.controller;

import com.taxi.notification.dto.NotificationRequestDto;
import com.taxi.notification.dto.NotificationResponseDto;
import com.taxi.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(
            @Valid @RequestBody NotificationRequestDto request) {
        log.info("POST /notifications - Creating notification for trip: {}", request.getTripId());
        NotificationResponseDto response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsByTrip(
            @RequestParam(name = "trip_id") Long tripId) {
        log.info("GET /notifications?trip_id={} - Fetching notifications", tripId);
        List<NotificationResponseDto> notifications = notificationService.getNotificationsByTrip(tripId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        log.debug("GET /notifications/stats - Fetching queue statistics");
        Map<String, Long> stats = notificationService.getQueueStats();
        return ResponseEntity.ok(stats);
    }
}