package com.taxi.user.controller;

import com.taxi.user.dto.PassengerDto;
import com.taxi.user.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsPassenger(@PathVariable Long id) {
        log.debug("GET /passengers/{}/exists - Checking if passenger exists", id);
        boolean exists = passengerService.existsPassenger(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerDto> getPassenger(@PathVariable Long id) {
        log.info("GET /passengers/{} - Fetching passenger", id);
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null
                || !a.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PASSENGER"))
                || !a.getName().equals(String.valueOf(id))) {
            throw new AccessDeniedException("Passenger profile access denied");
        }
        PassengerDto passenger = passengerService.getPassenger(id);
        return ResponseEntity.ok(passenger);
    }
}