package com.taxi.vehicle.controller;

import com.taxi.vehicle.dto.PriceEstimateResponseDto;
import com.taxi.vehicle.dto.VehicleRequestDto;
import com.taxi.vehicle.dto.VehicleResponseDto;
import com.taxi.vehicle.service.VehicleBusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class VehicleController {
    private final VehicleBusinessService vehicleBusinessService;

    @PostMapping("/vehicles")
    public ResponseEntity<VehicleResponseDto> create(@Valid @RequestBody VehicleRequestDto dto) {
        VehicleResponseDto created = vehicleBusinessService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/vehicles/driver/{driverId}")
    public ResponseEntity<List<VehicleResponseDto>> byDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(vehicleBusinessService.listByDriver(driverId));
    }

    @PutMapping("/vehicles/{id}")
    public ResponseEntity<VehicleResponseDto> update(@PathVariable Long id, @Valid @RequestBody VehicleRequestDto dto) {
        return ResponseEntity.ok(vehicleBusinessService.update(id, dto));
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleBusinessService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles/price-estimate")
    public ResponseEntity<PriceEstimateResponseDto> priceEstimate(
            @RequestParam Long driverId,
            @RequestParam double distanceKm,
            @RequestParam(required = false) Long vehicleId) {
        return ResponseEntity.ok(vehicleBusinessService.estimatePrice(driverId, vehicleId, distanceKm));
    }

    @PutMapping("/vehicles/driver/{driverId}/today/{vehicleId}")
    public ResponseEntity<VehicleResponseDto> setToday(
            @PathVariable Long driverId,
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleBusinessService.setTodayVehicle(driverId, vehicleId));
    }

    @GetMapping("/vehicles/driver/{driverId}/today")
    public ResponseEntity<VehicleResponseDto> getToday(@PathVariable Long driverId) {
        return ResponseEntity.ok(vehicleBusinessService.getToday(driverId));
    }
}
