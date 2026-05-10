package com.taxi.trip.service;

import com.taxi.trip.dto.DailyTripStatsResponse;
import com.taxi.trip.model.TripStatus;
import com.taxi.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TripStatsService {

    private final TripRepository tripRepository;

    @Transactional(readOnly = true)
    public DailyTripStatsResponse dailyStats(LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();
        LocalDateTime start = d.atStartOfDay();
        LocalDateTime end = d.plusDays(1).atStartOfDay();
        long count = tripRepository.countCompletedWithPriceBetween(TripStatus.COMPLETED, start, end);
        Double avg = tripRepository.averagePriceCompletedBetween(TripStatus.COMPLETED, start, end);
        return new DailyTripStatsResponse(d, count, avg);
    }
}
