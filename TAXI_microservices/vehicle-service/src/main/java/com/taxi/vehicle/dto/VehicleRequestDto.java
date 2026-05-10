package com.taxi.vehicle.dto;

import com.taxi.vehicle.model.TariffTier;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequestDto {
    @NotNull
    private Long driverId;

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    @NotNull
    @Min(1950)
    @Max(2100)
    private Integer manufactureYear;

    @NotBlank
    private String color;

    @NotBlank
    private String licensePlate;

    @NotNull
    private TariffTier tariff;
}
