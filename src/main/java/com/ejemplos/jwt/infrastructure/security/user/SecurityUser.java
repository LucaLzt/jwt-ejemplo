package com.ejemplos.jwt.infrastructure.security.user;

import com.ejemplos.jwt.domain.enums.UserRole;
import com.ejemplos.jwt.domain.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adaptador (Wrapper) que convierte nuestro {@link User} de dominio en un
 * {@link UserDetails} comprensible para Spring Security.
 * <p>
 * <strong>Patrón Adapter:</strong><br>
 * Spring Security necesita métodos específicos (getAuthorities, isAccountNonExpired, etc.)
 * que nuestro modelo de dominio no tiene (ni debería tener, para mantenerse puro).
 * Esta clase envuelve al usuario y responde esas preguntas por él.
 * </p>
 */
public class SecurityUser implements UserDetails {

    private final User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    /** Permite recuperar el usuario de dominio original si lo necesitamos en un controlador. */
    public User getDomainUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        UserRole role = user.getRole();
        if (role == null) {
            return List.of();
        }
        // Spring Security espera que los roles tengan el prefijo "ROLE_" por convención
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
