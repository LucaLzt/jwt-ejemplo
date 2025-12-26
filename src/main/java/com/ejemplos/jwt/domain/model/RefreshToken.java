package com.ejemplos.jwt.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

@Getter
@Builder
public class RefreshToken {

    private final Long id;
    private final Long userId;
    private final String token;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final boolean revoked;

    private RefreshToken(Long id, Long userId, String token, Instant createdAt, Instant expiresAt, boolean revoked) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null.");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank.");
        }
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public static RefreshToken create(Long userId, String token, Instant expiresAt) {
        return new RefreshToken(
                null,
                userId,
                token,
                Instant.now(),
                expiresAt,
                false
        );
    }
}
