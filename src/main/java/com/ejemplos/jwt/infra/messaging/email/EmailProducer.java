package com.ejemplos.jwt.infra.messaging.email;

import com.ejemplos.jwt.features.recoverypassword.application.dto.RecoveryEmailDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Producer responsable de enviar mensajes a RabbitMQ para que
 * se procese el envío del correo de recuperación de contraseña.
 *
 * Este componente NO envía correos directamente.
 * Solamente publica mensajes en la queue correspondiente.
 *
 * La lógica real del envío del email está en el consumer.
 *
 * Beneficios del enfoque asincrónico:
 *  - La petición HTTP responde inmediatamente.
 *  - El correo se envía en background.
 *  - El sistema es más escalable, resiliente y desacoplado.
 */
@Service
@RequiredArgsConstructor
public class EmailProducer {

    @Value("${rabbitmq.email.reset.exchange}")
    private String emailRecoveryPasswordExchange;

    @Value("${rabbitmq.email.reset.routing-key}")
    private String emailRecoveryPasswordRoutingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailProducer.class);

    /**
     * RabbitTemplate es la clase que Spring usa para enviar mensajes a RabbitMQ.
     *
     * Aquí ya viene configurado con:
     *  - JSON converter
     *  - Publisher confirms
     *  - Returns callback
     * (configurado en RabbitInfraConfig)
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica un mensaje en RabbitMQ para enviar un email de recuperación.
     *
     * Este método:
     *  1. Crea un DTO con información del email
     *  2. Genera un CorrelationData con un UUID (para tracking en confirms)
     *  3. Publica el mensaje en el exchange usando convertAndSend
     *
     * El consumer se encargará de:
     *  - Recibirlo
     *  - Intentar enviar el email
     *  - Aplicar reintentos automáticos si falla
     *  - Mandar a DLQ si falla permanentemente
     */
    public void sendEmailPasswordReset(String to, String link) {

        // Armamos el payload que viajará a la queue como JSON
        RecoveryEmailDTO recoveryEmailDTO = new RecoveryEmailDTO(to, link);

        // Correlation ID para publisher confirms (tracking del mensaje)
        var id = UUID.randomUUID().toString();

        LOGGER.info(String.format("Sending email password reset to: %s", to));


        rabbitTemplate.convertAndSend(
                emailRecoveryPasswordExchange,
                emailRecoveryPasswordRoutingKey,
                recoveryEmailDTO,
                new CorrelationData(id)
        );
    }

}
