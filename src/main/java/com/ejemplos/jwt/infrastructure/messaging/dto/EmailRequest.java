package com.ejemplos.jwt.infrastructure.messaging.dto;

public record EmailRequest(
        String to,
        String link
) {
}
