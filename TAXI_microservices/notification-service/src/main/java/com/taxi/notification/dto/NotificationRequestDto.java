package com.taxi.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Recipient type is required")
    private String recipientType;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Message is required")
    private String message;
}