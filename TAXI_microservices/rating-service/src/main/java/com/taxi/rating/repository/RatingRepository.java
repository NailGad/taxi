package com.taxi.rating.repository;

import com.taxi.rating.dto.AggregateRatingDto;
import com.taxi.rating.dto.TopDriverEntry;
import com.taxi.rating.model.Rating;
import com.taxi.rating.model.RatingDirection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByTripIdAndDirection(Long tripId, RatingDirection direction);

    List<Rating> findByTripId(Long tripId);

    @Query("SELECT new com.taxi.rating.dto.AggregateRatingDto(AVG(r.score), COUNT(r)) FROM Rating r WHERE r.rateeId = :id AND r.direction = :direction")
    Optional<AggregateRatingDto> aggregateForRatee(@Param("id") Long id, @Param("direction") RatingDirection direction);

    @Query("SELECT new com.taxi.rating.dto.TopDriverEntry(r.rateeId, AVG(r.score), COUNT(r)) FROM Rating r WHERE r.direction = :dir GROUP BY r.rateeId ORDER BY AVG(r.score) DESC")
    List<TopDriverEntry> findTopDrivers(@Param("dir") RatingDirection direction, Pageable pageable);
}
