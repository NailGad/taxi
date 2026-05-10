package com.taxi.user;

import com.taxi.user.dto.DriverDto;
import com.taxi.user.model.DriverStatus;
import com.taxi.user.service.DriverService;
import com.taxi.user.support.RedisBackedIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIf(value = "com.taxi.user.support.TestEnv#dockerAvailable",
        disabledReason = "Docker required for Redis (Testcontainers)")
class RedisAvailableDriversDockerIT extends RedisBackedIntegrationTest {

    @Autowired
    private DriverService driverService;

    @Test
    @Transactional
    void availableDriversEmptyWhenAllOffline() {
        DriverDto d = new DriverDto();
        d.setName("Off Driver");
        d.setEmail("off@test.com");
        d.setPhone("+79001234001");
        d.setLicenseNumber("L-OFF-1");
        d.setPassword("password12");
        driverService.registerDriver(d);
        assertThat(driverService.findAvailableDriversList()).isEmpty();
    }

    @Test
    @Transactional
    void availableDriversIncludesOnlineDriver() {
        DriverDto d = new DriverDto();
        d.setName("On Driver");
        d.setEmail("on@test.com");
        d.setPhone("+79001234002");
        d.setLicenseNumber("L-ON-2");
        d.setPassword("password12");
        DriverDto created = driverService.registerDriver(d);
        driverService.updateDriverStatus(created.getId(), DriverStatus.ONLINE);
        assertThat(driverService.findAvailableDriversList()).hasSize(1);
        assertThat(driverService.findAvailableDriversList().get(0).getId()).isEqualTo(created.getId());
    }
}
