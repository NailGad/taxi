package com.taxi.trip.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class TripJwtTokens {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long!!";

    private TripJwtTokens() {
    }

    public static String bearerPassenger(long passengerId) {
        return "Bearer " + token(passengerId, "PASSENGER");
    }

    public static String bearerDriver(long driverId) {
        return "Bearer " + token(driverId, "DRIVER");
    }

    public static String token(long userId, String role) {
        byte[] bytes = SECRET.getBytes(StandardCharsets.UTF_8);
        var key = Keys.hmacShaKeyFor(bytes);
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 3600000))
                .signWith(key)
                .compact();
    }
}
