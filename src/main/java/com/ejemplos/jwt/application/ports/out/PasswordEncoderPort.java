package com.ejemplos.jwt.application.ports.out;

/**
 * Puerto de Salida (Output Port) para el manejo de contraseñas seguras.
 * Abstrae el algoritmo de hashing (ej: BCrypt, Argon2).
 */
public interface PasswordEncoderPort {

    /**
     * Genera un hash seguro a partir de una contraseña en texto plano.
     */
    String encode(String rawPassword);

    /**
     * Verifica si una contraseña en texto plano coincide con un hash guardado.
     */
    boolean matches(String rawPassword, String encodedPassword);
}
