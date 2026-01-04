package com.ejemplos.jwt.infrastructure.security.user;

import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio estándar de Spring Security para cargar usuarios desde la base de datos.
 * <p>
 * Es utilizado internamente por el AuthenticationManager para validar credenciales.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos en nuestro repositorio de dominio
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Devolvemos el adaptador
        return new SecurityUser(user);
    }
}
