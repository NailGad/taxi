package com.taxi.user.service;

import com.taxi.user.dto.PassengerDto;
import com.taxi.user.model.Passenger;
import com.taxi.user.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerService {
    private final PassengerRepository passengerRepository;

    @Transactional
    public PassengerDto registerPassenger(PassengerDto passengerDto) {
        log.info("Registering new passenger with email: {}", passengerDto.getEmail());

        if (passengerRepository.existsByEmail(passengerDto.getEmail())) {
            throw new RuntimeException("Passenger with email " + passengerDto.getEmail() + " already exists");
        }

        Passenger passenger = new Passenger();
        passenger.setName(passengerDto.getName());
        passenger.setEmail(passengerDto.getEmail());
        passenger.setPhone(passengerDto.getPhone());

        Passenger saved = passengerRepository.save(passenger);
        log.info("Passenger registered successfully with id: {}", saved.getId());

        return convertToDto(saved);
    }

    public PassengerDto getPassenger(Long id) {
        log.debug("Fetching passenger with id: {}", id);
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Passenger not found with id: " + id));
        return convertToDto(passenger);
    }

    public boolean existsPassenger(Long id) {
        return passengerRepository.existsById(id);
    }

    private PassengerDto convertToDto(Passenger passenger) {
        return new PassengerDto(
                passenger.getId(),
                passenger.getName(),
                passenger.getEmail(),
                passenger.getPhone()
        );
    }
}