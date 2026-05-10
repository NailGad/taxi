package com.taxi.rating.service;

import com.taxi.rating.client.TripServiceClient;
import com.taxi.rating.dto.*;
import com.taxi.rating.model.Rating;
import com.taxi.rating.model.RatingDirection;
import com.taxi.rating.model.TripParticipantRole;
import com.taxi.rating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingBusinessService {

    private final RatingRepository ratingRepository;
    private final TripServiceClient tripServiceClient;

    @Transactional
    public RatingResponseDto rateTrip(Long tripId, CreateRatingRequest req) {
        TripInfoDto trip = tripServiceClient.getTrip(tripId);
        if (!"COMPLETED".equalsIgnoreCase(trip.getStatus())) {
            throw new RuntimeException("Trip must be COMPLETED to rate");
        }
        if (trip.getDriverId() == null) {
            throw new RuntimeException("Trip has no driver assigned");
        }
        RatingDirection direction;
        Long rateeId;
        if (req.getRaterRole() == TripParticipantRole.PASSENGER) {
            if (!req.getRaterId().equals(trip.getPassengerId())) {
                throw new RuntimeException("Rater is not the passenger of this trip");
            }
            direction = RatingDirection.PASSENGER_TO_DRIVER;
            rateeId = trip.getDriverId();
        } else {
            if (!req.getRaterId().equals(trip.getDriverId())) {
                throw new RuntimeException("Rater is not the driver of this trip");
            }
            direction = RatingDirection.DRIVER_TO_PASSENGER;
            rateeId = trip.getPassengerId();
        }
        if (ratingRepository.findByTripIdAndDirection(tripId, direction).isPresent()) {
            throw new RuntimeException("This trip is already rated for this direction");
        }
        Rating saved = ratingRepository.save(Rating.builder()
                .tripId(tripId)
                .direction(direction)
                .score(req.getScore())
                .raterId(req.getRaterId())
                .rateeId(rateeId)
                .build());
        return map(saved);
    }

    public AggregateRatingDto driverRating(Long driverId) {
        return ratingRepository.aggregateForRatee(driverId, RatingDirection.PASSENGER_TO_DRIVER)
                .orElse(new AggregateRatingDto(null, 0L));
    }

    public AggregateRatingDto passengerRating(Long passengerId) {
        return ratingRepository.aggregateForRatee(passengerId, RatingDirection.DRIVER_TO_PASSENGER)
                .orElse(new AggregateRatingDto(null, 0L));
    }

    public List<RatingResponseDto> tripRatings(Long tripId) {
        return ratingRepository.findByTripId(tripId).stream().map(this::map).toList();
    }

    public List<TopDriverEntry> topDrivers() {
        return ratingRepository.findTopDrivers(RatingDirection.PASSENGER_TO_DRIVER, PageRequest.of(0, 10));
    }

    private RatingResponseDto map(Rating r) {
        return new RatingResponseDto(
                r.getId(),
                r.getTripId(),
                r.getDirection(),
                r.getScore(),
                r.getRaterId(),
                r.getRateeId(),
                r.getCreatedAt()
        );
    }
}
