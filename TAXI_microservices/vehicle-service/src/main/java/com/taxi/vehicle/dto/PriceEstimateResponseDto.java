package com.taxi.vehicle.dto;

import com.taxi.vehicle.model.TariffTier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceEstimateResponseDto {
    private Long vehicleId;
    private Long driverId;
    private TariffTier tariff;
    private double tariffPerKm;
    private double distanceKm;
    private double price;
}
