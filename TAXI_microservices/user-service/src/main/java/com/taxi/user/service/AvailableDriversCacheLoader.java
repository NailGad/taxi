package com.taxi.user.service;

import com.taxi.user.config.RedisCacheConfig;
import com.taxi.user.dto.DriverDto;
import com.taxi.user.model.Driver;
import com.taxi.user.model.DriverStatus;
import com.taxi.user.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AvailableDriversCacheLoader {

    private final DriverRepository driverRepository;

    @Cacheable(cacheNames = RedisCacheConfig.AVAILABLE_DRIVERS_CACHE, key = "'online'")
    public List<DriverDto> loadOnlineDrivers() {
        return driverRepository.findByStatusOrderByIdAsc(DriverStatus.ONLINE).stream()
                .map(AvailableDriversCacheLoader::toDto)
                .toList();
    }

    private static DriverDto toDto(Driver driver) {
        return new DriverDto(
                driver.getId(),
                driver.getName(),
                driver.getEmail(),
                driver.getPhone(),
                driver.getLicenseNumber(),
                driver.getStatus()
        );
    }
}
