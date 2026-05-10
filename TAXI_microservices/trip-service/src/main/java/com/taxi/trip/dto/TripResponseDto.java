package com.taxi.trip.dto;

import com.taxi.trip.model.TripStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripResponseDto {
    private Long id;
    private Long passengerId;
    private Long driverId;
    private String origin;
    private String destination;
    private TripStatus status;
    private Double price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}