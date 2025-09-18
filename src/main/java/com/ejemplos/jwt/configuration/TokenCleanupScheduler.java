package com.ejemplos.jwt.configuration;

import com.ejemplos.jwt.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Componente que maneja tareas programadas para la limpieza de tokens.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    /**
     * Tarea programada que se ejecuta cada 24 horas para eliminar tokens expirados.
     * Se ejecuta a las 2:00 AM todos los días.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        try {
            refreshTokenService.deleteExpiredTokens();
            log.info("Successfully cleaned up expired refresh tokens");
        } catch (Exception e) {
            log.error("Error cleaning up expired refresh tokens: {}", e.getMessage());
        }
    }
}
