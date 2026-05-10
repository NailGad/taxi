package com.taxi.user.service;

import com.taxi.user.config.RedisCacheConfig;
import com.taxi.user.dto.DriverDto;
import com.taxi.user.model.Driver;
import com.taxi.user.model.DriverStatus;
import com.taxi.user.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {
    private final DriverRepository driverRepository;
    private final AvailableDriversCacheLoader availableDriversCacheLoader;

    @Transactional
    @CacheEvict(cacheNames = RedisCacheConfig.AVAILABLE_DRIVERS_CACHE, allEntries = true)
    public DriverDto registerDriver(DriverDto driverDto) {
        log.info("Registering new driver with email: {}", driverDto.getEmail());

        if (driverRepository.existsByEmail(driverDto.getEmail())) {
            throw new RuntimeException("Driver with email " + driverDto.getEmail() + " already exists");
        }

        if (driverRepository.existsByLicenseNumber(driverDto.getLicenseNumber())) {
            throw new RuntimeException("Driver with license number " + driverDto.getLicenseNumber() + " already exists");
        }

        Driver driver = new Driver();
        driver.setName(driverDto.getName());
        driver.setEmail(driverDto.getEmail());
        driver.setPhone(driverDto.getPhone());
        driver.setLicenseNumber(driverDto.getLicenseNumber());
        driver.setStatus(DriverStatus.OFFLINE); // Initially offline

        Driver saved = driverRepository.save(driver);
        log.info("Driver registered successfully with id: {}", saved.getId());

        return convertToDto(saved);
    }

    public DriverDto getDriver(Long id) {
        log.debug("Fetching driver with id: {}", id);
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));
        return convertToDto(driver);
    }

    @Transactional
    @CacheEvict(cacheNames = RedisCacheConfig.AVAILABLE_DRIVERS_CACHE, allEntries = true)
    public DriverDto updateDriverStatus(Long id, DriverStatus newStatus) {
        log.info("Updating driver {} status to: {}", id, newStatus);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));

        driver.setStatus(newStatus);
        Driver updated = driverRepository.save(driver);
        log.info("Driver {} status updated to: {}", id, newStatus);

        return convertToDto(updated);
    }

    public List<DriverDto> findAvailableDriversList() {
        return availableDriversCacheLoader.loadOnlineDrivers();
    }

    public Optional<DriverDto> findAvailableDriverDto() {
        List<DriverDto> list = findAvailableDriversList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsDriver(Long id) {
        return driverRepository.existsById(id);
    }

    private DriverDto convertToDto(Driver driver) {
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