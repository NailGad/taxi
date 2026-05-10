package com.taxi.user.service;

import com.taxi.user.dto.LoginRequest;
import com.taxi.user.dto.TokenResponse;
import com.taxi.user.model.Driver;
import com.taxi.user.model.Passenger;
import com.taxi.user.model.UserRole;
import com.taxi.user.repository.DriverRepository;
import com.taxi.user.repository.PassengerRepository;
import com.taxi.user.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public TokenResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (request.getRole() == UserRole.PASSENGER) {
            Passenger p = passengerRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
            if (!passwordEncoder.matches(request.getPassword(), p.getPasswordHash())) {
                throw new BadCredentialsException("Invalid email or password");
            }
            String token = jwtTokenService.createAccessToken(p.getId(), "PASSENGER");
            return new TokenResponse(token, "Bearer", p.getId(), "PASSENGER");
        }
        Driver d = driverRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), d.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        String token = jwtTokenService.createAccessToken(d.getId(), "DRIVER");
        return new TokenResponse(token, "Bearer", d.getId(), "DRIVER");
    }
}
