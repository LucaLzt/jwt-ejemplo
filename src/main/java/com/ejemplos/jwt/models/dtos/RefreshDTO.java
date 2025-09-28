package com.ejemplos.jwt.models.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) para almacenar la información necesaria para refrescar el token de acceso.
 */
@Getter @Setter
public class RefreshDTO {
    String refreshToken;
}
