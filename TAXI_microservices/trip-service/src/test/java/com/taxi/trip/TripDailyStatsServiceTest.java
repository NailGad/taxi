package com.taxi.trip;

import com.taxi.trip.model.Trip;
import com.taxi.trip.model.TripStatus;
import com.taxi.trip.repository.TripRepository;
import com.taxi.trip.service.TripStatsService;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TripStatsService.class)
@ActiveProfiles("test")
class TripDailyStatsServiceTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripStatsService tripStatsService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void dailyStatsEmptyWhenNoCompletedTrips() {
        var stats = tripStatsService.dailyStats(LocalDate.of(2030, 1, 15));
        assertThat(stats.getTripCount()).isZero();
        assertThat(stats.getAveragePrice()).isNull();
    }

    @Test
    void countsCompletedTripsWithPriceOnGivenDay() {
        LocalDate day = LocalDate.of(2030, 2, 1);
        LocalDateTime t1 = day.atTime(10, 0);
        LocalDateTime t2 = day.atTime(18, 30);
        persistCompleted(t1, 100.0);
        persistCompleted(t2, 200.0);

        var stats = tripStatsService.dailyStats(day);
        assertThat(stats.getTripCount()).isEqualTo(2);
        assertThat(stats.getAveragePrice()).isEqualTo(150.0);
    }

    @Test
    void ignoresPendingAndTripsWithoutPrice() {
        LocalDate day = LocalDate.of(2030, 3, 1);
        Trip pending = baseTrip();
        pending.setStatus(TripStatus.PENDING);
        pending.setPrice(null);
        tripRepository.saveAndFlush(pending);
        forceUpdatedAt(pending.getId(), day.atTime(12, 0));

        Trip completedNoPrice = baseTrip();
        completedNoPrice.setStatus(TripStatus.COMPLETED);
        completedNoPrice.setPrice(null);
        tripRepository.saveAndFlush(completedNoPrice);
        forceUpdatedAt(completedNoPrice.getId(), day.atTime(13, 0));

        Trip ok = baseTrip();
        ok.setStatus(TripStatus.COMPLETED);
        ok.setPrice(50.0);
        tripRepository.saveAndFlush(ok);
        forceUpdatedAt(ok.getId(), day.atTime(14, 0));

        var stats = tripStatsService.dailyStats(day);
        assertThat(stats.getTripCount()).isEqualTo(1);
        assertThat(stats.getAveragePrice()).isEqualTo(50.0);
    }

    private void persistCompleted(LocalDateTime updatedAt, double price) {
        Trip t = baseTrip();
        t.setStatus(TripStatus.COMPLETED);
        t.setPrice(price);
        tripRepository.saveAndFlush(t);
        forceUpdatedAt(t.getId(), updatedAt);
    }

    private Trip baseTrip() {
        Trip t = new Trip();
        t.setPassengerId(1L);
        t.setOrigin("A");
        t.setDestination("B");
        t.setStatus(TripStatus.PENDING);
        return t;
    }

    private void forceUpdatedAt(Long id, LocalDateTime updatedAt) {
        entityManager.createNativeQuery("UPDATE trips SET updated_at = ? WHERE id = ?")
                .setParameter(1, Timestamp.valueOf(updatedAt))
                .setParameter(2, id)
                .executeUpdate();
        entityManager.flush();
    }
}
