package com.ejemplos.jwt.domain.model;

import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Representa la llave maestra de la sesión actual del usuario.
 * <p>
 * A diferencia del Access Token (que es stateless y desechable), el Refresh Token
 * tiene <strong>estado</strong> y vive en la base de datos. Esto nos permite tener control
 * total sobre la sesión: podemos pausarla, revocarla o detectarla si es robada.
 * </p>
 * <p>
 * <strong>Concepto: Rotación (Token Rotation)</strong>
 * Esta entidad implementa la lógica para que los tokens sean de "un solo uso".
 * Cuando se usa, no se borra; se marca como 'revocado' y se apunta al nuevo token
 * que lo reemplazó ({@code replacedBy}).
 * </p>
 *
 * @author Luca
 */
@Getter
@Builder
public class RefreshToken {

    private final Long id;

    /**
     * ID del usuario dueño de este token.
     * No guardamos la entidad 'User' completa para evitar cargas perezosas (Lazy loading) innecesarias
     * y mantener el modelo ligero.
     */
    private final Long userId;

    /**
     * El token opaco real (generalmente un UUID o un JWT firmado).
     * Es lo que viaja en la cookie o header del cliente.
     */
    private final String token;


    private final Instant createdAt;

    /**
     * Fecha de muerte natural del token.
     * Un token puede morir por dos causas:
     * 1. Vejez (expiresAt < now)
     * 2. Muerte súbita (revoked = true)
     */
    private final Instant expiresAt;

    /**
     * Indica si el token ha sido invalidado prematuramente (Logout o Rotación).
     * Si esto es true, el token ya no sirve, aunque la fecha {@code expiresAt} sea futura.
     */
    private boolean revoked;

    /**
     * <strong>El Rastro de Migajas (Audit Trail)</strong>.
     * <p>
     * Cuando rotamos un token (Token A -> Token B), guardamos en el Token A: "Fui reemplazado por el Token B".
     * <br>
     * ¿Por qué? Si un hacker intenta usar el Token A <i>después</i> de que ya fue rotado,
     * el sistema verá que ya tiene un sucesor y activará la alarma de <strong>Robo de Identidad</strong>.
     * </p>
     */
    @Setter
    private String replacedBy;

    // Constructor completo
    private RefreshToken(Long id, Long userId, String token, Instant createdAt, Instant expiresAt, boolean revoked, String replacedBy) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null.");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank.");
        }
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.replacedBy = replacedBy;
    }

    // ========================================================================
    // Factory Method
    // ========================================================================

    /**
     * Crea un Refresh Token virgen y válido.
     *
     * @param userId    El dueño de la sesión.
     * @param token     El string seguro generado por el proveedor.
     * @param expiresAt Cuánto tiempo vivirá esta sesión (Ej.: 2 semanas).
     * @return Una instancia lista para ser guardada.
     */
    public static RefreshToken create(Long userId, String token, Instant expiresAt) {
        return new RefreshToken(
                null,
                userId,
                token,
                Instant.now(),
                expiresAt,
                false, // Nace vivo (no revocado)
                null           // Nace sin sucesor
        );
    }

    // ========================================================================
    // Domain Logic (Comportamiento)
    // ========================================================================

    /**
     * Ejecuta la <strong>Rotación del Token</strong>.
     * <p>
     * Invalida esta instancia actual y establece quién es su heredero.
     * </p>
     *
     * @param newRefreshToken El token nuevo que reemplaza a este.
     * @throws InvalidTokenException Si intentas rotar un token que ya estaba muerto (posible ataque).
     */
    public void rotate(String newRefreshToken) {
        if (this.revoked) {
            throw new InvalidTokenException("Attempted to rotate an already revoked token.");
        }
        this.revoked = true;
        this.replacedBy = newRefreshToken;
    }

    /**
     * "Mata" el token naturalmente.
     * Útil para Logout o cuando un administrador banea al usuario.
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * Verifica si el token es usable.
     * Para ser válido debe cumplir dos condiciones:
     * 1. No estar revocado (muerte súbita)-
     * 2. No haber expirado (muerte natural).
     */
    public boolean isValid() {
        return !this.revoked && !isExpired();
    }

    /**
     * Helper para verificar expiración por tiempo.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    /**
     * Detecta si el token está comprometido.
     * <p>
     * Un token está "comprometido" si tiene un sucesor (replacedBy != null)
     * pero alguien lo está intentando usar de nuevo.
     * </p>
     */
    public boolean isCompromised() {
        return this.revoked;
    }
}
