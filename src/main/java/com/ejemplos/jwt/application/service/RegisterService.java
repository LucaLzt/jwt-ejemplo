package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.RegisterCommand;
import com.ejemplos.jwt.application.ports.in.RegisterUseCase;
import com.ejemplos.jwt.application.ports.out.PasswordEncoderPort;
import com.ejemplos.jwt.domain.exception.personalized.EmailAlreadyExistsException;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado del registro de nuevos usuarios (Sign Up).
 * <p>
 * Su responsabilidad es orquestar la creación de una nueva identidad en el sistema,
 * asegurando que se cumplan las reglas de unicidad y seguridad desde el primer momento.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public User register(RegisterCommand command) {

        // 1. Validación de Regla de Negocio: Unicidad del Email
        // No confiamos solo en la restricción 'UNIQUE' de la base de datos;
        // lo validamos explícitamente para lanzar una excepción de dominio controlada.
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        // 2. Seguridad: Hashing de Contraseña
        // Nunca guardamos la contraseña en texto plano. Delegamos la encriptación al puerto.
        String encodedPassword = passwordEncoderPort.encode(command.password());

        // 3. Creación de la Entidad (Factory Method)
        // Usamos User.create() en lugar de new User() para garantizar que el usuario
        // nazca con los valores por defecto correctos (Rol CLIENT, Habilitado, Fechas, etc).
        User user = User.create(
                command.firstName(),
                command.lastName(),
                command.email(),
                encodedPassword
        );

        return userRepository.save(user);
    }
}
