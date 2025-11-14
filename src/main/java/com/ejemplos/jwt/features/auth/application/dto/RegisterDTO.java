package com.ejemplos.jwt.features.auth.application.dto;

import com.ejemplos.jwt.features.auth.domain.enums.UserRole;
import lombok.Data;

/**
 * DTO (Data Transfer Object) para almacenar la información necesaria para registrar un nuevo usuario.
 */
@Data
public class RegisterDTO {
    String name;
    String lastName;
    String email;
    String password;
    UserRole role;
}
