package com.taxi.vehicle.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TariffTier {
    STANDARD(10.0),
    COMFORT(15.0),
    BUSINESS(25.0);

    private final double coefficientPerKm;
}
