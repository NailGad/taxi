package com.taxi.user.controller;

import com.taxi.user.dto.DriverDto;
import com.taxi.user.dto.DriverStatusUpdateDto;
import com.taxi.user.model.DriverStatus;
import com.taxi.user.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverController {
    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverDto> registerDriver(@Valid @RequestBody DriverDto driverDto) {
        log.info("POST /drivers - Registering driver: {}", driverDto.getEmail());
        DriverDto created = driverService.registerDriver(driverDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DriverDto> updateDriverStatus(
            @PathVariable Long id,
            @Valid @RequestBody DriverStatusUpdateDto statusUpdate) {
        log.info("PATCH /drivers/{}/status - Updating status to: {}", id, statusUpdate.getStatus());
        DriverDto updated = driverService.updateDriverStatus(id, statusUpdate.getStatus());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/available")
    public ResponseEntity<DriverDto> findAvailableDriver() {
        log.info("GET /drivers/available - Finding available driver");
        Optional<DriverDto> driver = driverService.findAvailableDriverDto();
        return driver.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("No available drivers found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }

    @PostMapping("/claim-available")
    public ResponseEntity<DriverDto> claimAvailableDriver() {
        log.info("POST /drivers/claim-available - Atomically claiming next ONLINE driver");
        return driverService.claimAvailableDriver()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/available/list")
    public ResponseEntity<List<DriverDto>> listAvailableDrivers() {
        log.info("GET /drivers/available/list - Cached list of ONLINE drivers");
        return ResponseEntity.ok(driverService.findAvailableDriversList());
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsDriver(@PathVariable Long id) {
        log.debug("GET /drivers/{}/exists - Checking if driver exists", id);
        boolean exists = driverService.existsDriver(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverDto> getDriver(@PathVariable Long id) {
        log.info("GET /drivers/{} - Fetching driver", id);
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null
                || !a.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))
                || !a.getName().equals(String.valueOf(id))) {
            throw new AccessDeniedException("Driver profile access denied");
        }
        DriverDto driver = driverService.getDriver(id);
        return ResponseEntity.ok(driver);
    }
}