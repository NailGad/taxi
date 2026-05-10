package com.taxi.user;

import com.taxi.user.dto.PassengerDto;
import com.taxi.user.repository.PassengerRepository;
import com.taxi.user.service.PassengerService;
import com.taxi.user.support.RedisBackedIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnabledIf(value = "com.taxi.user.support.TestEnv#dockerAvailable",
        disabledReason = "Docker required for Redis (Testcontainers)")
@Transactional
class PassengerServiceRegistrationTest extends RedisBackedIntegrationTest {

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerPassengerPersistsHashedPassword() {
        PassengerDto dto = new PassengerDto();
        dto.setName("John Doe");
        dto.setEmail("john-reg@test.com");
        dto.setPhone("+79001234567");
        dto.setPassword("secretpass12");

        var saved = passengerService.registerPassenger(dto);
        assertThat(saved.getId()).isNotNull();
        var entity = passengerRepository.findById(saved.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("secretpass12", entity.getPasswordHash())).isTrue();
    }

    @Test
    void registerDuplicateEmailThrows() {
        PassengerDto dto = new PassengerDto();
        dto.setName("A");
        dto.setEmail("dup@test.com");
        dto.setPhone("+79001234561");
        dto.setPassword("secretpass12");
        passengerService.registerPassenger(dto);

        PassengerDto dto2 = new PassengerDto();
        dto2.setName("B");
        dto2.setEmail("dup@test.com");
        dto2.setPhone("+79001234562");
        dto2.setPassword("secretpass12");

        assertThatThrownBy(() -> passengerService.registerPassenger(dto2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }
}
