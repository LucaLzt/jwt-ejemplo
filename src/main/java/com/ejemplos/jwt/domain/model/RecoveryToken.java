package com.ejemplos.jwt.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class RecoveryToken {

    private Long id;
    private String token;
    private String email;
    private Instant expiresAt;
    private boolean used;

    public RecoveryToken(Long id, String token, String email, Instant expiresAt, boolean used) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
        this.used = used;
    }

    public static RecoveryToken create(String email, String token, int expirationSeconds) {
        return new RecoveryToken(
                null,
                token,
                email,
                Instant.now().plusSeconds(expirationSeconds),
                false
        );
    }

    public boolean isValid() {
        return !used && Instant.now().isBefore(expiresAt);
    }

    public void markAsUsed() {
        this.used = true;
    }

}
