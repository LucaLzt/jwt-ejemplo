package com.ejemplos.jwt.models.dtos;

import lombok.Data;

/**
 * DTO (Data Transfer Object) para almacenar la información necesaria para iniciar el flujo de recuperación de contraseña.
 */
@Data
public class ForgotDTO {
    private String email;
}
