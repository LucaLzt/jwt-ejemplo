package com.ejemplos.jwt.domain.model;

import lombok.Getter;

import java.time.Instant;

/**
 * Representa un Token JWT que ha sido invalidado explícitamente antes de su expiración natural.
 * <p>
 * <strong>Concepto: Lista Negra (Blacklist)</strong><br>
 * Los JWT son "stateless" (autocontenidos); una vez emitidos, el servidor no sabe si siguen vivos.
 * Para poder hacer "logout" o bloquear a un usuario inmediatamente, necesitamos guardar los identificadores (JTI)
 * de los tokens que ya no queremos aceptar en una "Lista Negra".
 * </p>
 */
@Getter
public class RevokedToken {

    private final Long id;

    /**
     * JTI (JWT ID): Identificador único del token.
     * Es el dato más importante, ya que el filtro de seguridad buscará este ID
     * para saber si rechazar la petición.
     */
    private final String jti;

    /**
     * El usuario dueño del token (normalmente el email).
     * Útil para auditoría: "¿Quién hizo logout?" o "¿A quién le revocamos sesión por seguridad?".
     */
    private final String subject;

    /**
     * Motivo de la revocación.
     * Ej.: "Logout voluntario", "Cambio de Rol", "Posible robo de identidad".
     */
    private final String reason;

    /**
     * Fecha en la que el token hubiera expirado naturalmente.
     * <p>
     * <strong>Optimización:</strong> Este campo es vital para la limpieza de la base de datos.
     * Un token revocado solo necesita estar en la blacklist hasta que expire.
     * Después de esa fecha, el token ya es inválido por sí mismo, así que podemos borrar el registro.
     * </p>
     */
    private final Instant expiresAt;

    private final Instant createdAt;

    // Constructor completo
    public RevokedToken(Long id, String jti, String subject, String reason, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.jti = jti;
        this.subject = subject;
        this.reason = reason;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    // ========================================================================
    // Factory Method
    // ========================================================================

    /**
     * Crea una nueva entrada de revocación.
     *
     * @param jti       El ID único del token que queremos matar.
     * @param subject   El email del usuario.
     * @param reason    La razón humana de por qué lo estamos invalidando.
     * @param expiresAt La fecha original de vencimiento del token (para saber cuándo borrar este registro).
     * @return Una instancia lista para ser persistida en la Blacklist.
     */
    public static RevokedToken revoke(String jti, String subject, String reason, Instant expiresAt) {
        return new RevokedToken(
                null,
                jti,
                subject,
                reason,
                expiresAt,
                Instant.now() // La revocación ocurre ahora
        );
    }
}
