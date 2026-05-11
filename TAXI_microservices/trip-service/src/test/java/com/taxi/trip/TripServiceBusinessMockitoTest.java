package com.taxi.trip;

import com.taxi.trip.client.UserServiceClient;
import com.taxi.trip.client.VehicleServiceClient;
import com.taxi.trip.dto.CreateTripRequest;
import com.taxi.trip.dto.DriverDto;
import com.taxi.trip.model.Trip;
import com.taxi.trip.model.TripStatus;
import com.taxi.trip.repository.TripRepository;
import com.taxi.trip.service.TripService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceBusinessMockitoTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private VehicleServiceClient vehicleServiceClient;

    @InjectMocks
    private TripService tripService;

    @Test
    void createTripFailsWhenPassengerMissing() {
        when(userServiceClient.checkPassengerExists(9L)).thenReturn(false);
        var req = new CreateTripRequest(9L, "A", "B");
        assertThatThrownBy(() -> tripService.createTrip(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Passenger not found");
    }

    @Test
    void createTripSavesWhenPassengerExists() {
        when(userServiceClient.checkPassengerExists(1L)).thenReturn(true);
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });
        when(tripRepository.findById(100L)).thenAnswer(inv -> {
            Trip t = new Trip();
            t.setId(100L);
            t.setPassengerId(1L);
            t.setOrigin("A");
            t.setDestination("B");
            t.setStatus(TripStatus.PENDING);
            return Optional.of(t);
        });
        when(userServiceClient.claimAvailableDriver()).thenReturn(Optional.empty());

        var dto = tripService.createTrip(new CreateTripRequest(1L, "A", "B"));

        assertThat(dto.getPassengerId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo(TripStatus.PENDING);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void completeTripRequiresPositiveDistance() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setDriverId(2L);
        trip.setStatus(TripStatus.IN_PROGRESS);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> tripService.updateTripStatus(1L, TripStatus.COMPLETED, 2L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("distanceKm");

        assertThatThrownBy(() -> tripService.updateTripStatus(1L, TripStatus.COMPLETED, 2L, -1.0))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void completeTripUsesVehicleClientForPrice() {
        Trip trip = new Trip();
        trip.setId(7L);
        trip.setDriverId(3L);
        trip.setVehicleId(10L);
        trip.setStatus(TripStatus.IN_PROGRESS);
        when(tripRepository.findById(7L)).thenReturn(Optional.of(trip));
        when(vehicleServiceClient.estimatePrice(3L, 10L, 5.0)).thenReturn(75.0);
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        tripService.updateTripStatus(7L, TripStatus.COMPLETED, 3L, 5.0);

        ArgumentCaptor<Trip> cap = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(cap.capture());
        assertThat(cap.getValue().getPrice()).isEqualTo(75.0);
        assertThat(cap.getValue().getDistanceKm()).isEqualTo(5.0);
        verify(userServiceClient).updateDriverStatus(3L, "ONLINE");
    }

    @Test
    void wrongDriverCannotUpdateTrip() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setDriverId(5L);
        trip.setStatus(TripStatus.ACCEPTED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> tripService.updateTripStatus(1L, TripStatus.IN_PROGRESS, 99L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not assigned");
    }

    @Test
    void createTripAssignsDriverWhenClaimSucceeds() {
        when(userServiceClient.checkPassengerExists(1L)).thenReturn(true);
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(200L);
            return t;
        });
        when(tripRepository.findById(200L)).thenAnswer(inv -> {
            Trip t = new Trip();
            t.setId(200L);
            t.setPassengerId(1L);
            t.setOrigin("A");
            t.setDestination("B");
            t.setStatus(TripStatus.ACCEPTED);
            t.setDriverId(3L);
            return Optional.of(t);
        });
        DriverDto driver = new DriverDto(3L, "D", "d@e.com", "+1234567890", "LIC", "BUSY");
        when(userServiceClient.claimAvailableDriver()).thenReturn(Optional.of(driver));
        when(vehicleServiceClient.findTodayVehicleId(3L)).thenReturn(Optional.of(10L));
        when(tripRepository.assignDriver(200L, 3L, 10L)).thenReturn(1);

        var dto = tripService.createTrip(new CreateTripRequest(1L, "A", "B"));

        assertThat(dto.getDriverId()).isEqualTo(3L);
        assertThat(dto.getStatus()).isEqualTo(TripStatus.ACCEPTED);
        verify(tripRepository).assignDriver(200L, 3L, 10L);
        verify(userServiceClient, never()).updateDriverStatus(eq(3L), eq("BUSY"));
    }

    @Test
    void createTripReleasesDriverWhenAssignFailsAfterClaim() {
        when(userServiceClient.checkPassengerExists(1L)).thenReturn(true);
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(201L);
            return t;
        });
        Trip pendingOut = new Trip();
        pendingOut.setId(201L);
        pendingOut.setPassengerId(1L);
        pendingOut.setOrigin("A");
        pendingOut.setDestination("B");
        pendingOut.setStatus(TripStatus.PENDING);
        when(tripRepository.findById(201L)).thenReturn(Optional.of(pendingOut));
        DriverDto driver = new DriverDto(4L, "D", "d2@e.com", "+1234567891", "LIC2", "BUSY");
        when(userServiceClient.claimAvailableDriver()).thenReturn(Optional.of(driver));
        when(vehicleServiceClient.findTodayVehicleId(4L)).thenReturn(Optional.empty());
        when(tripRepository.assignDriver(201L, 4L, null)).thenReturn(0);

        var dto = tripService.createTrip(new CreateTripRequest(1L, "A", "B"));

        assertThat(dto.getStatus()).isEqualTo(TripStatus.PENDING);
        verify(userServiceClient).updateDriverStatus(4L, "ONLINE");
    }
}
