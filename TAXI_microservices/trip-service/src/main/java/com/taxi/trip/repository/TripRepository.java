package com.taxi.trip.repository;

import com.taxi.trip.model.Trip;
import com.taxi.trip.model.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    List<Trip> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    Optional<Trip> findByIdAndPassengerId(Long id, Long passengerId);

    @Modifying
    @Query("UPDATE Trip t SET t.status = :status WHERE t.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") TripStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Trip t SET t.driverId = :driverId, t.vehicleId = :vehicleId, t.status = 'ACCEPTED' WHERE t.id = :id AND t.status = 'PENDING'")
    int assignDriver(@Param("id") Long id, @Param("driverId") Long driverId, @Param("vehicleId") Long vehicleId);
}