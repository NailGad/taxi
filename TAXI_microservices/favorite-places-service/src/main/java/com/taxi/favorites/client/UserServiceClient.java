package com.taxi.favorites.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${user-service.url}")
    private String userServiceUrl;

    public boolean passengerExists(Long passengerId) {
        return fetchExists(userServiceUrl + "/passengers/{id}/exists", passengerId);
    }

    public boolean driverExists(Long driverId) {
        return fetchExists(userServiceUrl + "/drivers/{id}/exists", driverId);
    }

    private boolean fetchExists(String uriTemplate, Long id) {
        try {
            Boolean exists = webClientBuilder.build()
                    .get()
                    .uri(uriTemplate, id)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(exists);
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.error("User exists check failed: {}", e.getMessage());
            return false;
        }
    }
}
