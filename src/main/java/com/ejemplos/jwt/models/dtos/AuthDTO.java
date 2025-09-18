package com.ejemplos.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) que representa la autenticación con tokens.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthDTO {
    private String accessToken;
    private String refreshToken;

    // Constructor para mantener compatibilidad
    public AuthDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}
