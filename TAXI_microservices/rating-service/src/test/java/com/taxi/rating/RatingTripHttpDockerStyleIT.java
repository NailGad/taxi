package com.taxi.rating;

import com.taxi.rating.dto.CreateRatingRequest;
import com.taxi.rating.model.TripParticipantRole;
import com.taxi.rating.repository.RatingRepository;
import com.taxi.rating.service.RatingBusinessService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RatingTripHttpDockerStyleIT {

    static final MockWebServer TRIP_API;

    static {
        TRIP_API = new MockWebServer();
        try {
            TRIP_API.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void tripUrl(DynamicPropertyRegistry registry) {
        registry.add("trip-service.url",
                () -> "http://127.0.0.1:" + TRIP_API.getPort());
    }

    @AfterAll
    static void shutdown() throws IOException {
        TRIP_API.shutdown();
    }

    @Autowired
    private RatingBusinessService ratingBusinessService;

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    void ratesTripUsingHttpTripServiceJson() {
        String body = "{\"id\":55,\"passengerId\":10,\"driverId\":20,\"status\":\"COMPLETED\"}";
        TRIP_API.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));

        var req = new CreateRatingRequest(5, TripParticipantRole.PASSENGER, 10L);
        var dto = ratingBusinessService.rateTrip(55L, req);

        assertThat(dto.getTripId()).isEqualTo(55L);
        assertThat(ratingRepository.count()).isEqualTo(1);
        assertThat(TRIP_API.getRequestCount()).isEqualTo(1);
    }

    @Test
    void secondRatingSameDirectionRejectedAfterHttpFetch() {
        String body = "{\"id\":56,\"passengerId\":11,\"driverId\":21,\"status\":\"COMPLETED\"}";
        TRIP_API.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));
        TRIP_API.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));

        ratingBusinessService.rateTrip(56L, new CreateRatingRequest(4, TripParticipantRole.PASSENGER, 11L));

        assertThatThrownBy(() -> ratingBusinessService.rateTrip(56L, new CreateRatingRequest(3, TripParticipantRole.PASSENGER, 11L)))
                .hasMessageContaining("already rated");
    }
}
