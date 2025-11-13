package com.ejemplos.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO (Data Transfer Object) que representa los datos necesarios para el envío de mails de recuperación de contraseña.
 */
@Data
@AllArgsConstructor
public class RecoveryEmailDTO {
    private String to;
    private String link;
}
