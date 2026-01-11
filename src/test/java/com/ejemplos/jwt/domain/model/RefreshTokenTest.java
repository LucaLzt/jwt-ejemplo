package com.ejemplos.jwt.domain.model;

import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenTest {

    @Test
    @DisplayName("Domain: Should create a valid refresh token with future expiration")
    void shouldCreateValidRefreshToken() {
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
    @DisplayName("Domain: Manual revocation should mark the token as invalid")
    void shouldRevokeTokenManually() {
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
    @DisplayName("Domain: Token rotation should invalidate the old token and link the replacement")
    void shouldRotateToken() {
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
    @DisplayName("Security: Attempting to rotate an already revoked token should throw exception (Reuse Detection)")
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
    @DisplayName("Domain: A token with a past expiration date should be considered invalid")
    void shouldCheckIfTokenIsExpired() {
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
