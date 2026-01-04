package com.ejemplos.jwt.domain.enums;

/**
 * Enumeración que define los roles de autorización disponibles en el sistema.
 * <p>
 * Se utiliza para la seguridad basada en roles (RBAC).
 * </p>
 */
public enum UserRole {
    /** Privilegios completos de administración. */
    ADMIN,

    /** Privilegios estándar con acceso limitado */
    CLIENT
}
