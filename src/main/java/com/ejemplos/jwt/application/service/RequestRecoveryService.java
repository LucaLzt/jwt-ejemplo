package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.RequestRecoveryUseCase;
import com.ejemplos.jwt.application.ports.out.EmailNotificationPort;
import com.ejemplos.jwt.domain.exception.personalized.UserNotFoundException;
import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.domain.repository.RecoveryTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio que inicia el flujo de recuperación de contraseña (Forgot Password).
 * <p>
 * Este servicio no cambia la contraseña, solo genera un "salvoconducto" temporal (Token)
 * y se lo envía al usuario legítimo por un canal seguro (Email).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RequestRecoveryService implements RequestRecoveryUseCase {

    private final UserRepository userRepository;
    private final RecoveryTokenRepository recoveryTokenRepository;
    private final EmailNotificationPort emailNotificationPort;

    @Override
    @Transactional
    public void requestRecovery(String email) {

        // 1. Verificación de existencia (Security through Obscurity?)
        // En sistemas muy seguros, a veces no se lanza error si el email no existe para no revelar usuarios.
        // Aquí decidimos ser explícitos para mejorar la UX del ejemplo.
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        // 2. Generación del Token Opaco
        // Usamos UUID porque es impredecible y único. No necesitamos un JWT aquí
        // porque el token se guarda en BD con estado (Stateful).
        String tokenString = UUID.randomUUID().toString();

        // 3. Creación de la entidad con expiración corta (15 min)
        RecoveryToken recoveryToken = RecoveryToken.create(
                email,
                tokenString,
                15 * 60 // 15 minutos en segundos
        );

        // 3. Creación de la entidad con expiración corta (15 min)
        recoveryTokenRepository.save(recoveryToken);

        // 4. Notificación Asíncrona (Opcional)
        // El puerto de email se encarga de enviar el link. En producción, esto
        // probablemente encole un mensaje en RabbitMQ para no bloquear la respuesta HTTP.
        String link = "https://miapp.com/recover?token=" + tokenString;
        emailNotificationPort.sendRecoveryEmail(email, link);
    }
}
