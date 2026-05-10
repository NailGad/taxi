package com.taxi.user;

import com.taxi.user.security.JwtTokenService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long!!";

    private final JwtTokenService jwtTokenService = new JwtTokenService(SECRET, 3600000L);

    @Test
    void createsTokenWithSubjectAndRole() {
        String token = jwtTokenService.createAccessToken(42L, "PASSENGER");
        var claims = jwtTokenService.parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("role", String.class)).isEqualTo("PASSENGER");
    }

    @Test
    void rejectsMalformedToken() {
        assertThatThrownBy(() -> jwtTokenService.parseClaims("not-a-jwt"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void rejectsSecretShorterThan32Bytes() {
        assertThatThrownBy(() -> new JwtTokenService("short", 3600000L))
                .isInstanceOf(IllegalStateException.class);
    }
}
