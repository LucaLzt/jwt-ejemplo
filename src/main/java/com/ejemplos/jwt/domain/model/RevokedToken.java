package com.ejemplos.jwt.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class RevokedToken {

    private final Long id;
    private final String jti;
    private final String subject;
    private final String reason;
    private final Instant expiresAt;
    private final Instant createdAt;

    public  RevokedToken(Long id, String jti, String subject, String reason, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.jti = jti;
        this.subject = subject;
        this.reason = reason;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static RevokedToken revoke(String jti, String subject, String reason, Instant expiresAt) {
        return new RevokedToken(
                null,
                jti,
                subject,
                reason,
                expiresAt,
                Instant.now()
        );
    }
}
