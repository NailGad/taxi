package com.taxi.trip.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
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
public class VehicleServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${vehicle-service.url}")
    private String vehicleServiceUrl;

    public Optional<Long> findTodayVehicleId(Long driverId) {
        try {
            VehicleBriefDto dto = webClientBuilder.build()
                    .get()
                    .uri(vehicleServiceUrl + "/vehicles/driver/{driverId}/today", driverId)
                    .retrieve()
                    .bodyToMono(VehicleBriefDto.class)
                    .block();
            return Optional.ofNullable(dto).map(VehicleBriefDto::getId);
        } catch (WebClientResponseException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Vehicle service today lookup failed for driver {}: {}", driverId, e.getMessage());
            return Optional.empty();
        }
    }

    public double estimatePrice(Long driverId, Long vehicleId, double distanceKm) {
        try {
            VehiclePriceEstimateDto dto = webClientBuilder.build()
                    .get()
                    .uri(priceEstimateUri(driverId, distanceKm, vehicleId))
                    .retrieve()
                    .bodyToMono(VehiclePriceEstimateDto.class)
                    .block();
            return dto != null ? dto.getPrice() : fallbackPrice(distanceKm);
        } catch (Exception e) {
            log.warn("Vehicle price estimate failed: {}", e.getMessage());
            return fallbackPrice(distanceKm);
        }
    }

    private String priceEstimateUri(Long driverId, double distanceKm, Long vehicleId) {
        StringBuilder sb = new StringBuilder(vehicleServiceUrl)
                .append("/vehicles/price-estimate?driverId=").append(driverId)
                .append("&distanceKm=").append(distanceKm);
        if (vehicleId != null) {
            sb.append("&vehicleId=").append(vehicleId);
        }
        return sb.toString();
    }

    private double fallbackPrice(double distanceKm) {
        return 50.0 + distanceKm;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class VehicleBriefDto {
        private Long id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class VehiclePriceEstimateDto {
        private double price;
    }
}
