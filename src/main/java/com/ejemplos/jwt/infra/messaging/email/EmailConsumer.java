package com.ejemplos.jwt.infra.messaging.email;

import com.ejemplos.jwt.features.recoverypassword.application.dto.RecoveryEmailDTO;
import com.ejemplos.jwt.shared.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Consumer responsable de recibir mensajes desde RabbitMQ
 * relacionados al envío de correos de recuperación de contraseña.
 *
 * Este componente:
 *   - Escucha la queue asociada a email reset password.
 *   - Recibe objetos RecoveryEmailDTO (Rabbit los deserializa automáticamente).
 *   - Llama al servicio EmailService para procesar el envío real del correo.
 *
 * El procesamiento es ASINCRÓNICO y ocurre fuera del flujo HTTP,
 * lo que hace al sistema más rápido, escalable y resiliente.
 */
@Service
@RequiredArgsConstructor
public class EmailConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailConsumer.class);

    private final EmailService emailService;

    /**
     * Listener que consume mensajes de la cola definida.
     *
     * Esta anotación hace que:
     *   - Spring cree un listener container
     *   - Se conecte a la queue
     *   - Use el MessageConverter para JSON
     *   - Entregue cada mensaje a este método
     *
     * IMPORTANTE: concurrency = "4"
     *
     * Esto permite que existan HASTA 4 consumidores paralelos del mismo método.
     * Ventajas:
     *   - Se procesan varios correos al mismo tiempo.
     *   - Ideal para alta carga o mucha concurrencia.
     *
     * Si algún email falla:
     *   - Se aplican reintentos automáticos (configurados en RabbitInfraConfig).
     *   - Si agota los reintentos → se rechaza → va a DLX → llega a la DLQ.
     */
    @RabbitListener(
            queues =  {"${rabbitmq.email.reset.queue}"},
            concurrency = "4"
    )
    public void consumeEmailPasswordReset(RecoveryEmailDTO emailDTO) {
        LOGGER.info(String.format("Consuming email password reset for: %s", emailDTO.getTo()));
        emailService.sendPasswordReset(emailDTO.getTo(), emailDTO.getLink());
    }

}
