package com.taxi.rating.controller;

import com.taxi.rating.dto.AggregateRatingDto;
import com.taxi.rating.dto.CreateRatingRequest;
import com.taxi.rating.dto.RatingResponseDto;
import com.taxi.rating.dto.TopDriverEntry;
import com.taxi.rating.service.RatingBusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RatingController {
    private final RatingBusinessService ratingBusinessService;

    @PostMapping("/ratings/trip/{tripId}")
    public ResponseEntity<RatingResponseDto> rateTrip(
            @PathVariable Long tripId,
            @Valid @RequestBody CreateRatingRequest request) {
        RatingResponseDto dto = ratingBusinessService.rateTrip(tripId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/ratings/driver/{driverId}")
    public ResponseEntity<AggregateRatingDto> driverRating(@PathVariable Long driverId) {
        return ResponseEntity.ok(ratingBusinessService.driverRating(driverId));
    }

    @GetMapping("/ratings/passenger/{passengerId}")
    public ResponseEntity<AggregateRatingDto> passengerRating(@PathVariable Long passengerId) {
        return ResponseEntity.ok(ratingBusinessService.passengerRating(passengerId));
    }

    @GetMapping("/ratings/trip/{tripId}")
    public ResponseEntity<List<RatingResponseDto>> tripRatings(@PathVariable Long tripId) {
        return ResponseEntity.ok(ratingBusinessService.tripRatings(tripId));
    }

    @GetMapping("/top-drivers")
    public ResponseEntity<List<TopDriverEntry>> topDrivers() {
        return ResponseEntity.ok(ratingBusinessService.topDrivers());
    }
}
