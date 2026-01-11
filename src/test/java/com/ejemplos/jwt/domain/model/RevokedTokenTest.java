package com.ejemplos.jwt.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RevokedTokenTest {

    @Test
    @DisplayName("Domain: Should correctly create a RevokedToken entity with metadata")
    void shouldCreateRevokedToken() {
        // GIVEN
        String jti = "sample-jti";
        String subject = "test@demo.com",
                reason = "User requested logout";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        // WHEN
        RevokedToken revokedToken = RevokedToken.revoke(jti, subject, reason, expiresAt);

        // THEN
        assertThat(revokedToken).isNotNull();
        assertThat(revokedToken.getJti()).isEqualTo(jti);
        assertThat(revokedToken.getSubject()).isEqualTo(subject);
        assertThat(revokedToken.getReason()).isEqualTo(reason);
        assertThat(revokedToken.getExpiresAt()).isAfter(Instant.now());
    }
}
