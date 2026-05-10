package com.taxi.vehicle;

import com.taxi.vehicle.client.UserServiceClient;
import com.taxi.vehicle.dto.VehicleRequestDto;
import com.taxi.vehicle.model.TariffTier;
import com.taxi.vehicle.model.Vehicle;
import com.taxi.vehicle.repository.VehicleRepository;
import com.taxi.vehicle.service.VehicleBusinessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleBusinessServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private VehicleBusinessService service;

    private VehicleRequestDto baseReq(long driverId, String brand, int year, TariffTier tier) {
        VehicleRequestDto dto = new VehicleRequestDto();
        dto.setDriverId(driverId);
        dto.setBrand(brand);
        dto.setModel("X");
        dto.setManufactureYear(year);
        dto.setColor("black");
        dto.setLicensePlate("Z" + driverId + "-" + year);
        dto.setTariff(tier);
        return dto;
    }

    @Test
    void createsComfortForRecentToyota() {
        when(userServiceClient.driverExists(1L)).thenReturn(true);
        when(vehicleRepository.countByDriverId(1L)).thenReturn(0L);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(50L);
            return v;
        });

        var dto = service.create(baseReq(1L, "Toyota", 2024, TariffTier.COMFORT));
        assertThat(dto.getId()).isEqualTo(50L);
        assertThat(dto.getTariff()).isEqualTo(TariffTier.COMFORT);
    }

    @Test
    void rejectsComfortWhenVehicleTooOld() {
        when(userServiceClient.driverExists(1L)).thenReturn(true);
        when(vehicleRepository.countByDriverId(1L)).thenReturn(0L);
        assertThatThrownBy(() -> service.create(baseReq(1L, "Lada", 2000, TariffTier.COMFORT)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("STANDARD");
    }

    @Test
    void rejectsBusinessForNonPremiumBrand() {
        when(userServiceClient.driverExists(1L)).thenReturn(true);
        when(vehicleRepository.countByDriverId(1L)).thenReturn(0L);
        assertThatThrownBy(() -> service.create(baseReq(1L, "Toyota", 2024, TariffTier.BUSINESS)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("BUSINESS");
    }

    @Test
    void allowsBusinessForBmw() {
        when(userServiceClient.driverExists(1L)).thenReturn(true);
        when(vehicleRepository.countByDriverId(1L)).thenReturn(0L);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(88L);
            return v;
        });
        var dto = service.create(baseReq(1L, "BMW", 2023, TariffTier.BUSINESS));
        assertThat(dto.getTariff()).isEqualTo(TariffTier.BUSINESS);
    }

    @Test
    void rejectsFourthVehicle() {
        when(userServiceClient.driverExists(1L)).thenReturn(true);
        when(vehicleRepository.countByDriverId(1L)).thenReturn(3L);
        assertThatThrownBy(() -> service.create(baseReq(1L, "Audi", 2022, TariffTier.STANDARD)))
                .hasMessageContaining("3");
    }

    @Test
    void rejectsUnknownDriver() {
        when(userServiceClient.driverExists(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.create(baseReq(99L, "VW", 2022, TariffTier.STANDARD)))
                .hasMessageContaining("Driver not found");
    }

    @Test
    void estimatePriceMultipliesDistanceByCoefficient() {
        Vehicle v = Vehicle.builder()
                .id(1L).driverId(2L).tariff(TariffTier.STANDARD)
                .brand("k").model("m").manufactureYear(2020).color("c").licensePlate("p").activeToday(false).build();
        when(vehicleRepository.findByDriverIdAndActiveTodayTrue(2L)).thenReturn(Optional.of(v));

        var est = service.estimatePrice(2L, null, 4.0);
        assertThat(est.getPrice()).isEqualTo(40.0);
        assertThat(est.getTariffPerKm()).isEqualTo(10.0);
    }

    @Test
    void estimatePriceRejectsNonPositiveDistance() {
        assertThatThrownBy(() -> service.estimatePrice(1L, null, 0))
                .hasMessageContaining("distanceKm");
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(vehicleRepository.existsById(7L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(7L)).hasMessageContaining("not found");
    }

    @Test
    void updateRejectsOwnerChangeInPayload() {
        Vehicle existing = Vehicle.builder()
                .id(10L).driverId(1L).tariff(TariffTier.STANDARD)
                .brand("Toyota").model("Camry").manufactureYear(2021).color("w").licensePlate("A").activeToday(false).build();
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(existing));

        VehicleRequestDto dto = baseReq(2L, "Toyota", 2021, TariffTier.STANDARD);
        dto.setLicensePlate("A");
        assertThatThrownBy(() -> service.update(10L, dto))
                .hasMessageContaining("Cannot change vehicle owner");
    }

}
