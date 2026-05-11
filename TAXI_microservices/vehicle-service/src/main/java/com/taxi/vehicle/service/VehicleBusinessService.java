package com.taxi.vehicle.service;

import com.taxi.vehicle.client.UserServiceClient;
import com.taxi.vehicle.dto.PriceEstimateResponseDto;
import com.taxi.vehicle.dto.VehicleRequestDto;
import com.taxi.vehicle.dto.VehicleResponseDto;
import com.taxi.vehicle.model.TariffTier;
import com.taxi.vehicle.model.Vehicle;
import com.taxi.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleBusinessService {

    private static final int MAX_VEHICLES_PER_DRIVER = 3;
    private static final Set<String> BUSINESS_ALLOWED_BRANDS = Set.of(
            "BMW", "MERCEDES", "AUDI", "MERCEDES-BENZ", "MERCEDES BENZ"
    );

    private final VehicleRepository vehicleRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public VehicleResponseDto create(VehicleRequestDto dto) {
        ensureDriverExists(dto.getDriverId());
        validateNewVehicleSlot(dto.getDriverId());
        validateTariffRules(dto.getBrand(), dto.getManufactureYear(), dto.getTariff());

        Vehicle v = Vehicle.builder()
                .driverId(dto.getDriverId())
                .brand(trim(dto.getBrand()))
                .model(trim(dto.getModel()))
                .manufactureYear(dto.getManufactureYear())
                .color(trim(dto.getColor()))
                .licensePlate(trim(dto.getLicensePlate()))
                .tariff(dto.getTariff())
                .activeToday(false)
                .build();
        return map(vehicleRepository.save(v));
    }

    public java.util.List<VehicleResponseDto> listByDriver(Long driverId) {
        return vehicleRepository.findByDriverIdOrderByIdAsc(driverId).stream().map(this::map).toList();
    }

    public VehicleResponseDto findById(Long id) {
        return vehicleRepository.findById(id)
                .map(this::map)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + id));
    }

    @Transactional
    public VehicleResponseDto update(Long id, VehicleRequestDto dto) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + id));
        if (!dto.getDriverId().equals(v.getDriverId())) {
            throw new RuntimeException("Cannot change vehicle owner");
        }
        validateTariffRules(dto.getBrand(), dto.getManufactureYear(), dto.getTariff());
        v.setBrand(trim(dto.getBrand()));
        v.setModel(trim(dto.getModel()));
        v.setManufactureYear(dto.getManufactureYear());
        v.setColor(trim(dto.getColor()));
        v.setLicensePlate(trim(dto.getLicensePlate()));
        v.setTariff(dto.getTariff());
        return map(vehicleRepository.save(v));
    }

    @Transactional
    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new RuntimeException("Vehicle not found: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    @Transactional
    public VehicleResponseDto setTodayVehicle(Long driverId, Long vehicleId) {
        ensureDriverExists(driverId);
        Vehicle target = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        if (!target.getDriverId().equals(driverId)) {
            throw new RuntimeException("Vehicle does not belong to driver");
        }
        for (Vehicle v : vehicleRepository.findByDriverId(driverId)) {
            v.setActiveToday(v.getId().equals(vehicleId));
            vehicleRepository.save(v);
        }
        return vehicleRepository.findById(vehicleId).map(this::map)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
    }

    public Optional<VehicleResponseDto> getToday(Long driverId) {
        return vehicleRepository.findByDriverIdAndActiveTodayTrue(driverId).map(this::map);
    }

    public Optional<Long> findTodayVehicleId(Long driverId) {
        return vehicleRepository.findByDriverIdAndActiveTodayTrue(driverId).map(Vehicle::getId);
    }

    public PriceEstimateResponseDto estimatePrice(Long driverId, Long vehicleIdOptional, double distanceKm) {
        if (distanceKm <= 0) {
            throw new RuntimeException("distanceKm must be positive");
        }
        Vehicle v = resolveVehicleForPricing(driverId, vehicleIdOptional);
        double coef = v.getTariff().getCoefficientPerKm();
        double price = coef * distanceKm;
        return new PriceEstimateResponseDto(v.getId(), driverId, v.getTariff(), coef, distanceKm, price);
    }

    private Vehicle resolveVehicleForPricing(Long driverId, Long vehicleIdOptional) {
        if (vehicleIdOptional != null) {
            Vehicle v = vehicleRepository.findById(vehicleIdOptional)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleIdOptional));
            if (!v.getDriverId().equals(driverId)) {
                throw new RuntimeException("Vehicle does not belong to driver");
            }
            return v;
        }
        return vehicleRepository.findByDriverIdAndActiveTodayTrue(driverId)
                .orElseGet(() -> vehicleRepository.findByDriverIdOrderByIdAsc(driverId).stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No vehicles for driver: " + driverId)));
    }

    private void ensureDriverExists(Long driverId) {
        if (!userServiceClient.driverExists(driverId)) {
            throw new RuntimeException("Driver not found: " + driverId);
        }
    }

    private void validateNewVehicleSlot(Long driverId) {
        long cnt = vehicleRepository.countByDriverId(driverId);
        if (cnt >= MAX_VEHICLES_PER_DRIVER) {
            throw new RuntimeException("Driver cannot have more than " + MAX_VEHICLES_PER_DRIVER + " vehicles");
        }
    }

    private void validateTariffRules(String brand, int manufactureYear, TariffTier requested) {
        int age = Year.now().getValue() - manufactureYear;
        if (age >= 10 && requested != TariffTier.STANDARD) {
            throw new RuntimeException("Vehicles aged 10+ years can only use STANDARD tariff");
        }
        if (requested == TariffTier.BUSINESS) {
            if (!isBusinessApprovedBrand(brand)) {
                throw new RuntimeException("BUSINESS tariff is only allowed for BMW, Mercedes, or Audi");
            }
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeBrand(String brand) {
        return trim(brand).toUpperCase(Locale.ROOT).replace('-', ' ').replaceAll("\\s+", " ");
    }

    private boolean isBusinessApprovedBrand(String brand) {
        String n = normalizeBrand(brand);
        if (BUSINESS_ALLOWED_BRANDS.contains(n)) {
            return true;
        }
        String first = n.split(" ", 2)[0];
        return BUSINESS_ALLOWED_BRANDS.stream().anyMatch(b -> first.equals(b) || first.equals(b.split(" ", 2)[0]));
    }

    private VehicleResponseDto map(Vehicle v) {
        return new VehicleResponseDto(
                v.getId(),
                v.getDriverId(),
                v.getBrand(),
                v.getModel(),
                v.getManufactureYear(),
                v.getColor(),
                v.getLicensePlate(),
                v.getTariff(),
                v.isActiveToday(),
                v.getCreatedAt(),
                v.getUpdatedAt()
        );
    }
}
