package com.ejemplos.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO (Data Transfer Object) que representa la autenticación con token de acceso y refresco.
 */
@Data @AllArgsConstructor
public class AuthDTO {
    String accessToken;
    String refreshToken;
}
