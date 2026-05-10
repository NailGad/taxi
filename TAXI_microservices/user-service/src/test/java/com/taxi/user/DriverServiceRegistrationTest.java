package com.taxi.user;

import com.taxi.user.dto.DriverDto;
import com.taxi.user.repository.DriverRepository;
import com.taxi.user.service.DriverService;
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
class DriverServiceRegistrationTest extends RedisBackedIntegrationTest {

    @Autowired
    private DriverService driverService;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private DriverDto sample(String email, String lic) {
        DriverDto d = new DriverDto();
        d.setName("Driver One");
        d.setEmail(email);
        d.setPhone("+79001234567");
        d.setLicenseNumber(lic);
        d.setPassword("secretpass12");
        return d;
    }

    @Test
    void registerDriverStoresPasswordHash() {
        var saved = driverService.registerDriver(sample("drv1@test.com", "LIC-100"));
        assertThat(saved.getId()).isNotNull();
        var entity = driverRepository.findById(saved.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("secretpass12", entity.getPasswordHash())).isTrue();
    }

    @Test
    void duplicateEmailRejected() {
        driverService.registerDriver(sample("same@test.com", "LIC-200"));
        assertThatThrownBy(() -> driverService.registerDriver(sample("same@test.com", "LIC-201")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("email");
    }

    @Test
    void duplicateLicenseRejected() {
        driverService.registerDriver(sample("e1@test.com", "LIC-300"));
        DriverDto d2 = sample("e2@test.com", "LIC-300");
        assertThatThrownBy(() -> driverService.registerDriver(d2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("license");
    }
}
