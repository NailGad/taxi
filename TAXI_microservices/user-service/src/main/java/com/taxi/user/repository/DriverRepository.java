package com.taxi.user.repository;

import com.taxi.user.model.Driver;
import com.taxi.user.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmail(String email);
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);

    List<Driver> findByStatus(DriverStatus status);

    List<Driver> findByStatusOrderByIdAsc(DriverStatus status);

    @Query("SELECT d FROM Driver d WHERE d.status = :status ORDER BY d.id ASC")
    Optional<Driver> findFirstByStatus(@Param("status") DriverStatus status);
}