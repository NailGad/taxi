package com.taxi.notification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long id;
    private Long tripId;
    private String recipientType;
    private Long recipientId;
    private String message;
    private String status;
    private Integer attempts;
    private String errorMessage;
    private LocalDateTime createdAt;
}