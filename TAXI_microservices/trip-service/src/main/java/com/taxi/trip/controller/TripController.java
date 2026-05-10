package com.taxi.trip.controller;

import com.taxi.trip.dto.CreateTripRequest;
import com.taxi.trip.dto.DailyTripStatsResponse;
import com.taxi.trip.dto.TripResponseDto;
import com.taxi.trip.dto.UpdateTripStatusRequest;
import com.taxi.trip.model.TripStatus;
import com.taxi.trip.service.TripService;
import com.taxi.trip.service.TripStatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {
    private final TripService tripService;
    private final TripStatsService tripStatsService;

    @PostMapping
    public ResponseEntity<TripResponseDto> createTrip(@Valid @RequestBody CreateTripRequest request) {
        long pid = requirePassengerId();
        if (!request.getPassengerId().equals(pid)) {
            throw new AccessDeniedException("Token does not match passengerId");
        }
        log.info("POST /trips - Creating trip for passenger: {}", request.getPassengerId());
        TripResponseDto trip = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<DailyTripStatsResponse> dailyStats(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        requirePassengerOrDriver();
        return ResponseEntity.ok(tripStatsService.dailyStats(date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDto> getTrip(@PathVariable Long id) {
        log.info("GET /trips/{} - Fetching trip", id);
        TripResponseDto trip = tripService.getTrip(id);
        return ResponseEntity.ok(trip);
    }

    @GetMapping
    public ResponseEntity<List<TripResponseDto>> getTrips(@RequestParam(name = "passenger_id") Long passengerId) {
        long pid = requirePassengerId();
        if (!passengerId.equals(pid)) {
            throw new AccessDeniedException("Token does not match passenger_id");
        }
        log.info("GET /trips?passenger_id={} - Fetching passenger trip history", passengerId);
        List<TripResponseDto> trips = tripService.getTripsByPassenger(passengerId);
        return ResponseEntity.ok(trips);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TripResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTripStatusRequest request,
            @RequestHeader(value = "X-Driver-Id", required = false) Long driverId) {
        long tokenDriverId = requireDriverId();
        if (driverId != null && !driverId.equals(tokenDriverId)) {
            throw new AccessDeniedException("X-Driver-Id does not match token");
        }
        log.info("PATCH /trips/{}/status - Updating status to: {}", id, request.getStatus());
        TripResponseDto trip = tripService.updateTripStatus(id, request.getStatus(), tokenDriverId, request.getDistanceKm());
        return ResponseEntity.ok(trip);
    }

    private long requirePassengerId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PASSENGER"))) {
            throw new AccessDeniedException("Passenger JWT required");
        }
        return Long.parseLong(a.getName());
    }

    private long requireDriverId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            throw new AccessDeniedException("Driver JWT required");
        }
        return Long.parseLong(a.getName());
    }

    private void requirePassengerOrDriver() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) {
            throw new AccessDeniedException("JWT required");
        }
        boolean ok = a.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PASSENGER"))
                || a.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"));
        if (!ok) {
            throw new AccessDeniedException("Passenger or driver JWT required");
        }
    }
}