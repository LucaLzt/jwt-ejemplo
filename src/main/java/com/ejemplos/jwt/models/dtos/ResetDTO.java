package com.ejemplos.jwt.models.dtos;

import lombok.Data;

/**
 * DTO (Data Transfer Object) para almacenar la información necesaria para recuperar la contraseña.
 */
@Data
public class ResetDTO {
    private String token;
    private String newPassword;
    private String repeatNewPassword;
}
