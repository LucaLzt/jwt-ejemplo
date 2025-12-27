package com.ejemplos.jwt.domain.model;

import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Builder
public class RefreshToken {

    private final Long id;
    private final Long userId;
    private final String token;
    private final Instant createdAt;
    private final Instant expiresAt;
    private boolean revoked;
    @Setter
    private String replacedBy;

    private RefreshToken(Long id, Long userId, String token, Instant createdAt, Instant expiresAt, boolean revoked, String replacedBy) {
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
        this.replacedBy = replacedBy;
    }

    public static RefreshToken create(Long userId, String token, Instant expiresAt) {
        return new RefreshToken(
                null,
                userId,
                token,
                Instant.now(),
                expiresAt,
                false,
                null
        );
    }

    public void rotate(String newRefreshToken) {
        if (this.revoked) {
            throw new InvalidTokenException("Attempted to rotate an already revoked token.");
        }
        this.revoked = true;
        this.replacedBy = newRefreshToken;
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isValid() {
        return !this.revoked && !isExpired();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isCompromised() {
        return this.revoked;
    }
}
