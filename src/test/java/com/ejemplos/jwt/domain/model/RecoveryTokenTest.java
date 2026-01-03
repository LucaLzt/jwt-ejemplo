package com.ejemplos.jwt.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RecoveryTokenTest {

    @Test
    void shouldBeCreateValidToken() {
        // GIVEN
        String email = "test@demo.com";
        String tokenString = "sample-token";
        int expirationSeconds = 600;

        // WHEN
        RecoveryToken token = RecoveryToken.create(email, tokenString, expirationSeconds);

        // THEN
        assertThat(token).isNotNull();
        assertThat(token.getEmail()).isEqualTo(email);
        assertThat(token.getToken()).isEqualTo(tokenString);
        assertThat(token.isUsed()).isFalse();

        assertThat(token.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void shouldBeValidIfNotUsedAndNotExpired() {
        // GIVEN
        RecoveryToken token = RecoveryToken.create("test@demo.com", "sample-token", 600);

        // WHEN
        boolean isValid = token.isValid();

        // THEN
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldBeInvalidIfMarkedAsUsed() {
        // GIVEN
        RecoveryToken token = RecoveryToken.create("test@demo.com", "sample-token", 600);

        // WHEN
        token.markAsUsed();

        // THEN
        assertThat(token.isUsed()).isTrue();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    void shouldBeInvalidIfExpired() {
        // GIVEN
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);

        RecoveryToken expiredToken = new RecoveryToken(
                1L,
                "expired-token",
                "test@demo.com",
                yesterday,
                false
        );

        // WHEN
        boolean isValid = expiredToken.isValid();

        // THEN
        assertThat(isValid).isFalse();
    }
}
