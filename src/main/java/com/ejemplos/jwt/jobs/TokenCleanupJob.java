package com.ejemplos.jwt.jobs;

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

    /**
     * Elimina de la BD todos los tokens revocados que ya vencieron (expires_at < now).
     * Corre todos los días a las 03:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredRevokedTokens() {
        int removed = revokedTokenRepository.deleteAllExpired(Instant.now());
        System.out.println("[TokenCleanupJob] Revoked tokens limpiados: " + removed);
    }

}
