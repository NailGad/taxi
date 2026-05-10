package com.taxi.trip;

import com.taxi.trip.client.UserServiceClient;
import com.taxi.trip.model.Trip;
import com.taxi.trip.model.TripStatus;
import com.taxi.trip.repository.TripRepository;
import com.taxi.trip.service.TripService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TripVehiclePriceHttpDockerStyleIT {

    static final MockWebServer VEHICLE_API;

    static {
        VEHICLE_API = new MockWebServer();
        try {
            VEHICLE_API.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void vehicleBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("vehicle-service.url",
                () -> "http://127.0.0.1:" + VEHICLE_API.getPort());
    }

    @AfterAll
    static void stopServer() throws IOException {
        VEHICLE_API.shutdown();
    }

    @Autowired
    private TripService tripService;

    @Autowired
    private TripRepository tripRepository;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void completionFetchesPriceFromHttpVehicleService() {
        VEHICLE_API.enqueue(new MockResponse()
                .setBody("{\"price\":222.5}")
                .addHeader("Content-Type", "application/json"));

        when(userServiceClient.updateDriverStatus(anyLong(), anyString())).thenReturn(true);

        Trip trip = new Trip();
        trip.setPassengerId(1L);
        trip.setDriverId(40L);
        trip.setVehicleId(99L);
        trip.setOrigin("a");
        trip.setDestination("b");
        trip.setStatus(TripStatus.IN_PROGRESS);
        Trip saved = tripRepository.saveAndFlush(trip);

        tripService.updateTripStatus(saved.getId(), TripStatus.COMPLETED, 40L, 8.0);

        Trip updated = tripRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getPrice()).isEqualTo(222.5);
        assertThat(updated.getDistanceKm()).isEqualTo(8.0);
        assertThat(VEHICLE_API.getRequestCount()).isPositive();
    }
}
