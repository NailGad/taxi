package com.taxi.user.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.taxi.user.model.DriverStatus;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatusUpdateDto {
    @NotNull(message = "Status is required")
    private DriverStatus status;
}