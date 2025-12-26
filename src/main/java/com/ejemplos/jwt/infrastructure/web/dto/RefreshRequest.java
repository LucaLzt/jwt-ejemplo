package com.ejemplos.jwt.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "The refresh token must not be blank")
        String refreshToken
) {}
