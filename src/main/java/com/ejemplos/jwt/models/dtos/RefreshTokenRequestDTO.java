package com.ejemplos.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar un nuevo access token usando un refresh token.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequestDTO {
    private String refreshToken;
}
