package com.taxi.rating.client;

import com.taxi.rating.dto.TripInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripServiceClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${trip-service.url}")
    private String tripServiceUrl;

    public TripInfoDto getTrip(Long tripId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(tripServiceUrl + "/trips/{id}", tripId)
                    .retrieve()
                    .bodyToMono(TripInfoDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new RuntimeException("Trip not found: " + tripId);
        } catch (Exception e) {
            log.error("Error fetching trip {}: {}", tripId, e.getMessage());
            throw new RuntimeException("Cannot load trip " + tripId);
        }
    }
}
