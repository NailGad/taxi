package com.taxi.user.service;

import com.taxi.user.config.RedisCacheConfig;
import com.taxi.user.dto.DriverDto;
import com.taxi.user.model.Driver;
import com.taxi.user.model.DriverStatus;
import com.taxi.user.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @CacheEvict(cacheNames = RedisCacheConfig.AVAILABLE_DRIVERS_CACHE, allEntries = true)
    public DriverDto registerDriver(DriverDto driverDto) {
        String emailNorm = driverDto.getEmail().trim().toLowerCase();
        log.info("Registering new driver with email: {}", emailNorm);

        if (driverRepository.existsByEmail(emailNorm)) {
            throw new RuntimeException("Driver with email " + emailNorm + " already exists");
        }

        if (driverRepository.existsByLicenseNumber(driverDto.getLicenseNumber())) {
            throw new RuntimeException("Driver with license number " + driverDto.getLicenseNumber() + " already exists");
        }

        Driver driver = new Driver();
        driver.setName(driverDto.getName());
        driver.setEmail(emailNorm);
        driver.setPhone(driverDto.getPhone());
        driver.setLicenseNumber(driverDto.getLicenseNumber());
        driver.setStatus(DriverStatus.OFFLINE);
        driver.setPasswordHash(passwordEncoder.encode(driverDto.getPassword()));

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

    /**
     * Atomically reserves the lowest-id ONLINE driver: row lock + transition to BUSY in one transaction.
     * Trip-service should call this instead of GET /drivers/available + PATCH to avoid double-booking.
     */
    @Transactional
    @CacheEvict(cacheNames = RedisCacheConfig.AVAILABLE_DRIVERS_CACHE, allEntries = true)
    public Optional<DriverDto> claimAvailableDriver() {
        Optional<Driver> opt = driverRepository.findFirstByStatusOrderByIdAscForUpdate(DriverStatus.ONLINE);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Driver driver = opt.get();
        driver.setStatus(DriverStatus.BUSY);
        Driver saved = driverRepository.save(driver);
        log.info("Claimed driver {} for trip assignment (ONLINE -> BUSY)", saved.getId());
        return Optional.of(convertToDto(saved));
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