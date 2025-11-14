package com.ejemplos.jwt.features.auth.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) para almacenar la información necesaria para refrescar el token de acceso.
 */
@Getter @Setter
public class RefreshDTO {
    String refreshToken;
}
