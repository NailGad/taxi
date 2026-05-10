package com.taxi.trip.client;

import com.taxi.trip.dto.DriverDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${user-service.url}")
    private String userServiceUrl;

    public boolean checkPassengerExists(Long passengerId) {
        try {
            log.debug("Checking if passenger exists: {}", passengerId);
            Boolean exists = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/passengers/{id}/exists", passengerId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(exists);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Passenger {} not found", passengerId);
            return false;
        } catch (Exception e) {
            log.error("Error checking passenger {}: {}", passengerId, e.getMessage());
            return false;
        }
    }

    public Optional<DriverDto> findAvailableDriver() {
        try {
            log.debug("Finding available driver");
            DriverDto driver = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/drivers/available")
                    .retrieve()
                    .bodyToMono(DriverDto.class)
                    .block();
            return Optional.ofNullable(driver);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("No available driver found");
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error finding available driver: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean updateDriverStatus(Long driverId, String status) {
        try {
            log.debug("Updating driver {} status to {}", driverId, status);
            String requestBody = String.format("{\"status\":\"%s\"}", status);

            webClientBuilder.build()
                    .patch()
                    .uri(userServiceUrl + "/drivers/{id}/status", driverId)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Driver {} status updated to {}", driverId, status);
            return true;
        } catch (Exception e) {
            log.error("Error updating driver {} status: {}", driverId, e.getMessage());
            return false;
        }
    }
}