package com.ejemplos.jwt.domain.model;

import lombok.Getter;

import java.time.Instant;

/**
 * Reperesenta un token de seguridad para el flujo de "Olvidé mi contraseña".
 * <p>
 * <strong>Concepto: Token con Estado (Stateful Token)</strong><br>
 * A diferencia de los JWT de acceso (que son stateless), los tokens de recuperación
 * suelen persistirse en la base de datos.
 * <br>
 * ¿Por qué? Porque necesitamos garantizar estrictamente que sea de <strong>UN SOLO USO</strong>.
 * Una vez consumido para cambiar la contraseña, debe morir inmediatamente (markAsUsed),
 * incluso si su tiempo de vida original no ha terminado.
 * </p>
 *
 * @author Luca
 */
@Getter
public class RecoveryToken {

    private Long id;

    /**
     * El código secreto que se envía por correo (generalmente un UUID).
     * No necesita contener datos (claims) como un JWT, solo ser único y difícil de adivinar.
     */
    private String token;

    /**
     * El correo del usuario que solicitó la recuperación.
     * Vincula el token con una cuenta específica.
     */
    private String email;

    /**
     * Fecha límite para usar el token (Ventana de seguridad).
     * Generalmente es corta (ej.: 15 o 30 minutos) para reducir la superficie de ataque.
     */
    private Instant expiresAt;

    /**
     * Bandera crítica de seguridad.
     * Evita ataques de "Replay" (intentar usar el mismo link de correo dos veces).
     */
    private boolean used;

    // Constructor completo
    public RecoveryToken(Long id, String token, String email, Instant expiresAt, boolean used) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
        this.used = used;
    }

    // ========================================================================
    // Factory Method
    // ========================================================================

    /**
     * Genera un nuevo token de recuperación válido.
     *
     * @param email             A quién pertenece.
     * @param token             El string aleatorio seguro (UUID).
     * @param expirationSeconds Tiempo de vida en segundos (ej: 900 para 15 min).
     * @return Una instancia lista para ser enviada por email y guardada en BD.
     */
    public static RecoveryToken create(String email, String token, int expirationSeconds) {
        return new RecoveryToken(
                null,
                token,
                email,
                Instant.now().plusSeconds(expirationSeconds),
                false
        );
    }

    // ========================================================================
    // Domain Logic
    // ========================================================================

    /**
     * Valida si el token puede ser utilizado para cambiar la contraseña.
     * <p>
     * Regla de Negocio:
     * 1. No debe haber sido usado antes (Protección Replay).
     * 2. No debe haber expirado (Protección Temporal).
     * </p>
     *
     * @return true si es seguro proceder.
     */
    public boolean isValid() {
        return !used && Instant.now().isBefore(expiresAt);
    }

    /**
     * Quema el token después de un cambio de contraseña exitoso.
     * Esto es irreversible.
     */
    public void markAsUsed() {
        this.used = true;
    }
}
