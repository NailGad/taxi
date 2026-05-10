package com.taxi.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTripStatsResponse {
    private LocalDate date;
    private long tripCount;
    private Double averagePrice;
}
