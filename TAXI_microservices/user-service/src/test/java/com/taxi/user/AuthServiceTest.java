package com.taxi.user;

import com.taxi.user.dto.LoginRequest;
import com.taxi.user.model.Driver;
import com.taxi.user.model.DriverStatus;
import com.taxi.user.model.Passenger;
import com.taxi.user.model.UserRole;
import com.taxi.user.repository.DriverRepository;
import com.taxi.user.repository.PassengerRepository;
import com.taxi.user.security.JwtTokenService;
import com.taxi.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PassengerRepository passengerRepository;
    @Mock
    private DriverRepository driverRepository;

    private PasswordEncoder passwordEncoder;
    private JwtTokenService jwtTokenService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtTokenService = new JwtTokenService("test-secret-key-at-least-32-bytes-long!!", 3600000L);
        authService = new AuthService(passengerRepository, driverRepository, passwordEncoder, jwtTokenService);
    }

    @Test
    void loginPassengerSuccess() {
        Passenger p = new Passenger();
        p.setId(1L);
        p.setEmail("a@test.com");
        p.setPasswordHash(passwordEncoder.encode("password12"));
        when(passengerRepository.findByEmail("a@test.com")).thenReturn(Optional.of(p));

        var res = authService.login(new LoginRequest("a@test.com", "password12", UserRole.PASSENGER));
        assertThat(res.getUserId()).isEqualTo(1L);
        assertThat(res.getRole()).isEqualTo("PASSENGER");
        assertThat(res.getAccessToken()).isNotBlank();
    }

    @Test
    void loginDriverSuccess() {
        Driver d = new Driver();
        d.setId(2L);
        d.setEmail("d@test.com");
        d.setPasswordHash(passwordEncoder.encode("password12"));
        d.setStatus(DriverStatus.OFFLINE);
        when(driverRepository.findByEmail("d@test.com")).thenReturn(Optional.of(d));

        var res = authService.login(new LoginRequest("d@test.com", "password12", UserRole.DRIVER));
        assertThat(res.getUserId()).isEqualTo(2L);
        assertThat(res.getRole()).isEqualTo("DRIVER");
    }

    @Test
    void loginFailsWrongPassword() {
        Passenger p = new Passenger();
        p.setEmail("a@test.com");
        p.setPasswordHash(passwordEncoder.encode("other"));
        when(passengerRepository.findByEmail("a@test.com")).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> authService.login(new LoginRequest("a@test.com", "password12", UserRole.PASSENGER)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginFailsUnknownPassengerEmail() {
        when(passengerRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("x@test.com", "pw", UserRole.PASSENGER)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginFailsUnknownDriverEmail() {
        when(driverRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("x@test.com", "pw", UserRole.DRIVER)))
                .isInstanceOf(BadCredentialsException.class);
    }
}
