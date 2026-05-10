package com.taxi.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {
    @NotNull(message = "Passenger ID is required")
    private Long passengerId;

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;
}