package com.ejemplos.jwt.jobs;

import com.ejemplos.jwt.repositories.PasswordResetTokenRepository;
import com.ejemplos.jwt.repositories.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Limpia todos los tokens expirados de diferentes tipos
     * Corre todos los días a las 03:00 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredRevokedTokens() {
        // Limpio los tokens revocados o expirados
        int removed = revokedTokenRepository.deleteAllExpired(Instant.now());

        // Limpio los tokens de reseteo de contraseña expirados
        int removedPasswordReset = passwordResetTokenRepository.deleteAllUsedOrExpired(Instant.now());

        System.out.println("[TokenCleanupJob] Revoked tokens limpiados: " + removed + ", Password reset tokens limpiados: " + removedPasswordReset);
    }

}
