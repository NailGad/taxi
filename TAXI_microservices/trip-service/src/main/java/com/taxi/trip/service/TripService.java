package com.taxi.trip.service;

import com.taxi.trip.client.UserServiceClient;
import com.taxi.trip.dto.CreateTripRequest;
import com.taxi.trip.dto.DriverDto;
import com.taxi.trip.dto.TripResponseDto;
import com.taxi.trip.model.Trip;
import com.taxi.trip.model.TripStatus;
import com.taxi.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {
    private final TripRepository tripRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public TripResponseDto createTrip(CreateTripRequest request) {
        log.info("Creating trip for passenger: {}", request.getPassengerId());

        if (!userServiceClient.checkPassengerExists(request.getPassengerId())) {
            throw new RuntimeException("Passenger not found: " + request.getPassengerId());
        }

        Trip trip = new Trip();
        trip.setPassengerId(request.getPassengerId());
        trip.setOrigin(request.getOrigin());
        trip.setDestination(request.getDestination());
        trip.setStatus(TripStatus.PENDING);

        Trip savedTrip = tripRepository.save(trip);
        log.info("Trip created with id: {}", savedTrip.getId());

        assignDriverToTrip(savedTrip);

        return convertToDto(savedTrip);
    }

    @Transactional
    private void assignDriverToTrip(Trip trip) {
        log.info("Looking for available driver for trip: {}", trip.getId());

        Optional<DriverDto> availableDriver = userServiceClient.findAvailableDriver();

        if (availableDriver.isPresent()) {
            DriverDto driver = availableDriver.get();
            log.info("Found driver {} for trip {}", driver.getId(), trip.getId());

            int updated = tripRepository.assignDriver(trip.getId(), driver.getId());

            if (updated > 0) {
                // Обновляем статус водителя на BUSY
                userServiceClient.updateDriverStatus(driver.getId(), "BUSY");
                log.info("Driver {} assigned to trip {}", driver.getId(), trip.getId());
            } else {
                log.warn("Trip {} was already assigned to another driver", trip.getId());
            }
        } else {
            log.warn("No available driver found for trip {}", trip.getId());
        }
    }

    public TripResponseDto getTrip(Long id) {
        log.debug("Fetching trip: {}", id);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + id));
        return convertToDto(trip);
    }

    public List<TripResponseDto> getTripsByPassenger(Long passengerId) {
        log.debug("Fetching trips for passenger: {}", passengerId);
        List<Trip> trips = tripRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
        return trips.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TripResponseDto updateTripStatus(Long id, TripStatus newStatus, Long driverId) {
        log.info("Updating trip {} status to: {} by driver {}", id, newStatus, driverId);

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + id));

        if (driverId != null && trip.getDriverId() != null && !trip.getDriverId().equals(driverId)) {
            throw new RuntimeException("Driver " + driverId + " is not assigned to trip " + id);
        }

        TripStatus oldStatus = trip.getStatus();
        trip.setStatus(newStatus);

        switch (newStatus) {
            case ACCEPTED:
                log.info("Trip {} accepted by driver {}", id, driverId);
                break;

            case IN_PROGRESS:
                if (oldStatus != TripStatus.ACCEPTED) {
                    throw new RuntimeException("Cannot start trip that is not accepted");
                }
                log.info("Trip {} is in progress", id);
                break;

            case COMPLETED:
                if (oldStatus != TripStatus.IN_PROGRESS) {
                    throw new RuntimeException("Cannot complete trip that is not in progress");
                }
                if (trip.getDriverId() != null) {
                    userServiceClient.updateDriverStatus(trip.getDriverId(), "ONLINE");
                    log.info("Driver {} released back to ONLINE", trip.getDriverId());
                }
                trip.setPrice(calculatePrice(trip.getOrigin(), trip.getDestination()));
                log.info("Trip {} completed with price: {}", id, trip.getPrice());
                break;

            case CANCELLED:
                if (trip.getDriverId() != null && (oldStatus == TripStatus.PENDING || oldStatus == TripStatus.ACCEPTED)) {
                    userServiceClient.updateDriverStatus(trip.getDriverId(), "ONLINE");
                    log.info("Driver {} released back to ONLINE due to cancellation", trip.getDriverId());
                }
                log.info("Trip {} cancelled", id);
                break;

            default:
                break;
        }

        Trip updated = tripRepository.save(trip);
        return convertToDto(updated);
    }

    private Double calculatePrice(String origin, String destination) {
        return 50.0 + Math.random() * 100;
    }

    private TripResponseDto convertToDto(Trip trip) {
        return new TripResponseDto(
                trip.getId(),
                trip.getPassengerId(),
                trip.getDriverId(),
                trip.getOrigin(),
                trip.getDestination(),
                trip.getStatus(),
                trip.getPrice(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}