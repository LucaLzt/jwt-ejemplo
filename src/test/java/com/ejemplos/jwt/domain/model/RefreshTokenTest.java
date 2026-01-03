package com.ejemplos.jwt.domain.model;

import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RefreshTokenTest {

    @Test
    void shouldBeCreateValidRefreshToken() {
        // GIVEN
        Long userId = 1L;
        String token = "sample-refresh-token";
        int expiration = 600;

        // WHEN
        RefreshToken refreshToken = RefreshToken.create(
                userId,
                token,
                Instant.now().plusSeconds(expiration)
        );

        // THEN
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getUserId()).isEqualTo(userId);
        assertThat(refreshToken.getToken()).isEqualTo(token);
        assertThat(refreshToken.isRevoked()).isFalse();
        assertThat(refreshToken.isValid()).isTrue();
        assertThat(refreshToken.isCompromised()).isFalse();

        assertThat(refreshToken.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void shouldRevokedTokenManually() {
        // GIVEN
        Long userId = 1L;
        String token = "sample-refresh-token";
        int expiration = 600;

        RefreshToken refreshToken = RefreshToken.create(
                userId,
                token,
                Instant.now().plusSeconds(expiration)
        );

        // WHEN
        refreshToken.revoke();

        // THEN
        assertThat(refreshToken.isRevoked()).isTrue();
        assertThat(refreshToken.isValid()).isFalse();
    }

    @Test
    void shouldBeRotateToken() {
        // GIVEN
        Long userId = 1L;
        String token = "sample-refresh-token";
        int expiration = 600;

        RefreshToken refreshToken = RefreshToken.create(
                userId,
                token,
                Instant.now().plusSeconds(expiration)
        );

        String newRefreshToken = "new-refresh-token";

        // WHEN
        refreshToken.rotate(newRefreshToken);

        // THEN
        assertThat(refreshToken.isValid()).isFalse();
        assertThat(refreshToken.isRevoked()).isTrue();
        assertThat(refreshToken.getReplacedBy()).isEqualTo(newRefreshToken);
    }

    @Test
    void shouldThrowIfRotatingRevokedToken() {
        // GIVEN
        Long userId = 1L;
        String token = "sample-refresh-token";
        int expiration = 600;

        RefreshToken refreshToken = RefreshToken.create(
                userId,
                token,
                Instant.now().plusSeconds(expiration)
        );

        // WHEN
        refreshToken.revoke();

        // THEN
        assertThatThrownBy(() -> refreshToken.rotate(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Attempted to rotate an already revoked token.");
    }

    @Test
    void shouldBeCheckIfTokenIsExpired() {
        // GIVEN
        Long userId = 1L;
        String token = "sample-refresh-token";
        Instant yesterday = Instant.now().minusSeconds(86400);

        RefreshToken refreshToken = RefreshToken.create(
                userId,
                token,
                yesterday
        );

        // WHEN
        boolean isValid = refreshToken.isValid();

        // THEN
        assertThat(isValid).isFalse();
        assertThat(refreshToken.isExpired()).isTrue();
    }
}
