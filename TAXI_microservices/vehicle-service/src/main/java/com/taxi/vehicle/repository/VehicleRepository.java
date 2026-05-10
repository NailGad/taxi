package com.taxi.vehicle.repository;

import com.taxi.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    long countByDriverId(Long driverId);

    List<Vehicle> findByDriverIdOrderByIdAsc(Long driverId);

    Optional<Vehicle> findByDriverIdAndActiveTodayTrue(Long driverId);

    List<Vehicle> findByDriverId(Long driverId);
}
