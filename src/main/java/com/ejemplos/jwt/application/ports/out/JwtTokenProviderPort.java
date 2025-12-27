package com.ejemplos.jwt.application.ports.out;

import com.ejemplos.jwt.domain.model.User;
import org.springframework.security.core.Authentication;

import java.time.Instant;

public interface JwtTokenProviderPort {

    String generateAccessToken(User user);

    GeneratedToken generateRefreshToken(User user);

    boolean isAccessTokenValid(String token);

    boolean isRefreshTokenValid(String token);

    Authentication getAuthentication(String token);

    String getUsernameFromToken(String token);

    String getJtiFromToken(String token);

    Instant getExpirationFromToken(String token);
}
