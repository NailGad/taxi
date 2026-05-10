package com.taxi.user.controller;

import com.taxi.user.dto.PassengerDto;
import com.taxi.user.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor
@Slf4j
public class PassengerController {
    private final PassengerService passengerService;

    @PostMapping
    public ResponseEntity<PassengerDto> registerPassenger(@Valid @RequestBody PassengerDto passengerDto) {
        log.info("POST /passengers - Registering passenger: {}", passengerDto.getEmail());
        PassengerDto created = passengerService.registerPassenger(passengerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerDto> getPassenger(@PathVariable Long id) {
        log.info("GET /passengers/{} - Fetching passenger", id);
        PassengerDto passenger = passengerService.getPassenger(id);
        return ResponseEntity.ok(passenger);
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsPassenger(@PathVariable Long id) {
        log.debug("GET /passengers/{}/exists - Checking if passenger exists", id);
        boolean exists = passengerService.existsPassenger(id);
        return ResponseEntity.ok(exists);
    }
}