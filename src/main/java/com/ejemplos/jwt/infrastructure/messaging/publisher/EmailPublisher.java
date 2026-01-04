package com.ejemplos.jwt.infrastructure.messaging.publisher;

import com.ejemplos.jwt.infrastructure.messaging.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Publicador de mensajes para eventos de correo electrónico.
 * <p>
 * Se encarga de poner el mensaje en el Exchange correcto de RabbitMQ.
 * Utiliza 'CorrelationData' para poder rastrear la confirmación (ACK) del broker.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPublisher {

    @Value("${rabbitmq.email.reset.exchange}")
    private String emailRecoveryPasswordExchange;

    @Value("${rabbitmq.email.reset.routing-key}")
    private String emailRecoveryPasswordRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica un evento de solicitud de email de recuperación.
     * Es una operación asíncrona ("Fire and Forget" para el cliente HTTP).
     */
    public void sendEmailPasswordReset(String to, String link) {
        EmailRequest recoveryEmailDTO = new EmailRequest(to, link);
        var id = UUID.randomUUID().toString();

        log.info("Sending email password reset to: {}", to);

        rabbitTemplate.convertAndSend(
                emailRecoveryPasswordExchange,
                emailRecoveryPasswordRoutingKey,
                recoveryEmailDTO,
                new CorrelationData(id)// ID para tracking
        );
    }
}
