package com.taxi.rating;

import com.taxi.rating.client.TripServiceClient;
import com.taxi.rating.dto.AggregateRatingDto;
import com.taxi.rating.dto.CreateRatingRequest;
import com.taxi.rating.dto.TripInfoDto;
import com.taxi.rating.model.Rating;
import com.taxi.rating.model.RatingDirection;
import com.taxi.rating.model.TripParticipantRole;
import com.taxi.rating.repository.RatingRepository;
import com.taxi.rating.service.RatingBusinessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingBusinessServiceUnitTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private TripServiceClient tripServiceClient;

    @InjectMocks
    private RatingBusinessService service;

    private TripInfoDto completedTrip() {
        TripInfoDto t = new TripInfoDto();
        t.setId(1L);
        t.setPassengerId(10L);
        t.setDriverId(20L);
        t.setStatus("COMPLETED");
        return t;
    }

    @Test
    void passengerRatesDriverStoresRating() {
        when(tripServiceClient.getTrip(1L)).thenReturn(completedTrip());
        when(ratingRepository.findByTripIdAndDirection(1L, RatingDirection.PASSENGER_TO_DRIVER)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> {
            Rating r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });

        var req = new CreateRatingRequest(5, TripParticipantRole.PASSENGER, 10L);
        var dto = service.rateTrip(1L, req);

        assertThat(dto.getScore()).isEqualTo(5);
        assertThat(dto.getDirection()).isEqualTo(RatingDirection.PASSENGER_TO_DRIVER);

        ArgumentCaptor<Rating> cap = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(cap.capture());
        assertThat(cap.getValue().getRateeId()).isEqualTo(20L);
    }

    @Test
    void rejectsRatingWhenTripNotCompleted() {
        TripInfoDto t = completedTrip();
        t.setStatus("IN_PROGRESS");
        when(tripServiceClient.getTrip(1L)).thenReturn(t);

        assertThatThrownBy(() -> service.rateTrip(1L, new CreateRatingRequest(4, TripParticipantRole.PASSENGER, 10L)))
                .hasMessageContaining("COMPLETED");
    }

    @Test
    void rejectsPassengerRaterMismatch() {
        when(tripServiceClient.getTrip(1L)).thenReturn(completedTrip());

        assertThatThrownBy(() -> service.rateTrip(1L, new CreateRatingRequest(4, TripParticipantRole.PASSENGER, 999L)))
                .hasMessageContaining("passenger");
    }

    @Test
    void rejectsDriverRaterMismatch() {
        when(tripServiceClient.getTrip(1L)).thenReturn(completedTrip());

        assertThatThrownBy(() -> service.rateTrip(1L, new CreateRatingRequest(4, TripParticipantRole.DRIVER, 999L)))
                .hasMessageContaining("driver");
    }

    @Test
    void rejectsWhenTripHasNoDriver() {
        TripInfoDto t = completedTrip();
        t.setDriverId(null);
        when(tripServiceClient.getTrip(1L)).thenReturn(t);

        assertThatThrownBy(() -> service.rateTrip(1L, new CreateRatingRequest(4, TripParticipantRole.PASSENGER, 10L)))
                .hasMessageContaining("no driver");
    }

    @Test
    void rejectsDuplicatePassengerRating() {
        when(tripServiceClient.getTrip(1L)).thenReturn(completedTrip());
        when(ratingRepository.findByTripIdAndDirection(1L, RatingDirection.PASSENGER_TO_DRIVER))
                .thenReturn(Optional.of(new Rating()));

        assertThatThrownBy(() -> service.rateTrip(1L, new CreateRatingRequest(3, TripParticipantRole.PASSENGER, 10L)))
                .hasMessageContaining("already rated");
    }

    @Test
    void driverRatesPassenger() {
        when(tripServiceClient.getTrip(1L)).thenReturn(completedTrip());
        when(ratingRepository.findByTripIdAndDirection(1L, RatingDirection.DRIVER_TO_PASSENGER)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> {
            Rating r = inv.getArgument(0);
            r.setId(3L);
            return r;
        });

        var dto = service.rateTrip(1L, new CreateRatingRequest(4, TripParticipantRole.DRIVER, 20L));
        assertThat(dto.getDirection()).isEqualTo(RatingDirection.DRIVER_TO_PASSENGER);
    }

    @Test
    void driverAggregateDefaultsWhenEmpty() {
        when(ratingRepository.aggregateForRatee(5L, RatingDirection.PASSENGER_TO_DRIVER)).thenReturn(Optional.empty());
        AggregateRatingDto agg = service.driverRating(5L);
        assertThat(agg.getRatingsCount()).isZero();
        assertThat(agg.getAverageScore()).isNull();
    }

    @Test
    void passengerAggregateDelegates() {
        when(ratingRepository.aggregateForRatee(8L, RatingDirection.DRIVER_TO_PASSENGER))
                .thenReturn(Optional.of(new AggregateRatingDto(4.2, 7L)));
        assertThat(service.passengerRating(8L).getAverageScore()).isEqualTo(4.2);
    }

}
