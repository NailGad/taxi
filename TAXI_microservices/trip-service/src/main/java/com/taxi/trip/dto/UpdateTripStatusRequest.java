package com.taxi.trip.dto;

import com.taxi.trip.model.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripStatusRequest {
    @NotNull(message = "Status is required")
    private TripStatus status;
}