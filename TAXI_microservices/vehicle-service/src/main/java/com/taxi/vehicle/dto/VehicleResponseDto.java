package com.taxi.vehicle.dto;

import com.taxi.vehicle.model.TariffTier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDto {
    private Long id;
    private Long driverId;
    private String brand;
    private String model;
    private Integer manufactureYear;
    private String color;
    private String licensePlate;
    private TariffTier tariff;
    private boolean activeToday;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
