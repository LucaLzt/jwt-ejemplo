package com.ejemplos.jwt.services.impl;

import com.ejemplos.jwt.models.entities.PasswordResetToken;
import com.ejemplos.jwt.models.entities.User;
import com.ejemplos.jwt.repositories.PasswordResetTokenRepository;
import com.ejemplos.jwt.repositories.UserRepository;
import com.ejemplos.jwt.services.PasswordRecoveryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Implementación de los servicios de recuperación de contraseña del sistema.
 */
@Service
@RequiredArgsConstructor
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailService;

    // TTL del token de recuperación (15 minutos)
    private static final Duration TTL = Duration.ofMinutes(15);

    /**
     * Inicia el flujo de recuperación: genera un token y envía el email con el link.
     * Si el email no existe, no revela información (retorna igual).
     *
     * @param email Email del usuario que solicita recuperar la contraseña.
     * @param appBaseUrl URL base de la aplicación para armar el link (ej: http://localhost:8080).
     * @return void
     * @exception Exception si ocurre un error al enviar correo (según configuración SMTP).
     */
    @Transactional
    public void requestRecovery(String email, String appBaseUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with the provided email"));

        // Genero el token a través de UUID
        String rawToken = UUID.randomUUID().toString();

        // Persisto el token en la base de datos
        PasswordResetToken prt = new PasswordResetToken();
        prt.setUser(user);
        prt.setToken(rawToken);
        prt.setExpiresAt(Instant.now().plus(TTL));
        passwordResetTokenRepository.save(prt);

        // Armo el link
        String link = appBaseUrl + "/api/auth/reset-password?token=" + rawToken;

        // Envío el email
        emailService.sendPasswordReset(email, link);
    }

   /**
    * Aplica el reseteo de contraseña si el token es válido (existe, no expiró, no se usó).
    *
    * @param token Token UUID recibido desde el link del email.
    * @param newPassword Nueva contraseña en texto plano (se encripta).
    * @return void
    * @exception IllegalArgumentException si el token es inválido, vencido o ya usado.
    */
   @Transactional
   public void confirmReset(String token, String newPassword, String repeatNewPassword) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        if (prt.getUsedAt() != null || prt.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired password reset token");
        }

        // Verifico excepciones de la contraseña
        verifyPasswordRules(newPassword, repeatNewPassword);

        // Actualizo la contraseña del usuario e inválido refresh tokens
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRefreshToken(null);
        userRepository.save(user);

        // Marco el token como usado
        prt.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(prt);
   }

   /**
    * Verifica que la contraseña cumpla con un estándar.
    *
    * @param newPassword Nueva contraseña en texto plano.
    * @param repeatNewPassword Repetición de Nueva contraseña en texto plano.
    * @return void
    * @exception IllegalArgumentException si no cumple con alguna validación.
    */
   private void verifyPasswordRules(String newPassword, String repeatNewPassword) {
       if (newPassword == null || repeatNewPassword == null) {
           throw new IllegalArgumentException("Password cannot be null");
       }
       if (!newPassword.equals(repeatNewPassword)) {
           throw new IllegalArgumentException("Passwords do not match");
       }
       if (newPassword.length() < 8) {
           throw new IllegalArgumentException("Password must be at least 8 characters long");
       }
   }

}
