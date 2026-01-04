package com.ejemplos.jwt.domain.model;

import com.ejemplos.jwt.domain.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Representa la entidad central del Usuario en el dominio.
 * <p>
 * Esta clase sigue el principio de <strong>Modelo de Dominio Rico</strong> (Rich Domain Model),
 * donde la entidad no es solo un contenedor de datos (DTO), sino que también encapsula
 * reglas de negocio básicas como la modificación de sus propios roles.
 * </p>
 *
 * @author Luca
 */
@Getter
@Builder
public class User {

    private final Long id;
    private String firstName;
    private String lastName;
    private String email;

    /**
     * La contraseña se mantiene mutable (@Setter) para permitir casos de uso
     * como "Reset Password" sin recrear toda la entidad.
     */
    @Setter
    private String password;

    private UserRole role;

    /**
     * Indica si el usuario puede iniciar sesión.
     * Útil para implementar "Soft Deletes" o bloqueos temporales sin borrar el registro.
     */
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    // Constructor completo usado por Lombok y mappers (Infraestructura)
    public User(Long id, String firstName, String lastName, String email, String password, UserRole role, boolean enabled, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ========================================================================
    // Factory Methods (Creación Semántica)
    // ========================================================================

    /**
     * Factory Method para encapsular la creación de un nuevo usuario estándar.
     * <p>
     * Se utiliza en lugar del constructor directo para garantizar que todo usuario
     * nuevo nazca con valores coherentes por defecto:
     * </p>
     * <ul>
     *     <li>Rol: {@code CLIENT}</li>
     *     <li>Estado: {@code enabled = true}</li>
     *     <li>Rol: {@code Instant.now()}</li>
     * </ul>
     *
     * @param firstName Nombre real.
     * @param lastName  Apellido real.
     * @param email     Correo electrónico (debe ser validado previamente como único).
     * @param password  Contraseña (ya debe venir encriptada/hasheada).
     * @return Una nueva instancia válida de User lista para guardar.
     */
    public static User create(String firstName, String lastName, String email, String password) {
        return new User(
                null,
                firstName,
                lastName,
                email,
                password,
                UserRole.CLIENT,
                true,
                Instant.now(),
                Instant.now()
        );
    }

    // ========================================================================
    // Domain Logic (Comportamiento del Negocio)
    // ========================================================================

    /**
     * Alterna el rol del usuario (De ADMIN a CLIENT o viceversa).
     * <p>
     * Este método encapsula la regla de negocio del cambio de permisos,
     * asegurando que siempre se actualice la fecha de modificación (audit trail).
     * </p>
     */
    public void toggleRole() {
        if (this.role == UserRole.ADMIN) {
            this.role = UserRole.CLIENT;
        } else {
            this.role = UserRole.ADMIN;
        }
        // Auditoría: Es vital registrar cuándo ocurrió el último cambio
        this.updatedAt = Instant.now();
    }
}