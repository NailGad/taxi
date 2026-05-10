package com.taxi.rating.controller;

import com.taxi.rating.dto.AggregateRatingDto;
import com.taxi.rating.dto.CreateRatingRequest;
import com.taxi.rating.dto.RatingResponseDto;
import com.taxi.rating.dto.TopDriverEntry;
import com.taxi.rating.service.RatingBusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.taxi.rating.model.TripParticipantRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
        assertRater(request);
        RatingResponseDto dto = ratingBusinessService.rateTrip(tripId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    private void assertRater(CreateRatingRequest request) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String expected = request.getRaterRole() == TripParticipantRole.PASSENGER
                ? "ROLE_PASSENGER"
                : "ROLE_DRIVER";
        if (a == null || !a.getAuthorities().contains(new SimpleGrantedAuthority(expected))) {
            throw new AccessDeniedException("JWT role does not match raterRole");
        }
        if (!a.getName().equals(String.valueOf(request.getRaterId()))) {
            throw new AccessDeniedException("JWT subject does not match raterId");
        }
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
