package com.taxi.trip.controller;

import com.taxi.trip.dto.CreateTripRequest;
import com.taxi.trip.dto.TripResponseDto;
import com.taxi.trip.dto.UpdateTripStatusRequest;
import com.taxi.trip.model.TripStatus;
import com.taxi.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {
    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponseDto> createTrip(@Valid @RequestBody CreateTripRequest request) {
        log.info("POST /trips - Creating trip for passenger: {}", request.getPassengerId());
        TripResponseDto trip = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDto> getTrip(@PathVariable Long id) {
        log.info("GET /trips/{} - Fetching trip", id);
        TripResponseDto trip = tripService.getTrip(id);
        return ResponseEntity.ok(trip);
    }

    @GetMapping
    public ResponseEntity<List<TripResponseDto>> getTrips(@RequestParam(name = "passenger_id") Long passengerId) {
        log.info("GET /trips?passenger_id={} - Fetching passenger trip history", passengerId);
        List<TripResponseDto> trips = tripService.getTripsByPassenger(passengerId);
        return ResponseEntity.ok(trips);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TripResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTripStatusRequest request,
            @RequestHeader(value = "X-Driver-Id", required = false) Long driverId) {
        log.info("PATCH /trips/{}/status - Updating status to: {}", id, request.getStatus());
        TripResponseDto trip = tripService.updateTripStatus(id, request.getStatus(), driverId, request.getDistanceKm());
        return ResponseEntity.ok(trip);
    }
}