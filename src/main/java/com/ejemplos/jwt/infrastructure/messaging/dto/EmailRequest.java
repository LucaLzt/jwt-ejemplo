package com.ejemplos.jwt.infrastructure.messaging.dto;

/**
 * DTO (Data Transfer Object) para los mensajes de correo en la cola de RabbitMQ.
 * <p>
 * Representa la carga útil (payload) que viaja desde el Publicador hasta el Consumidor.
 * Se serializa automáticamente a JSON.
 * </p>
 */
public record EmailRequest(
        String to,
        String link
) {
}
