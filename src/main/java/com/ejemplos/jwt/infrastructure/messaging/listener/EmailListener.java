package com.ejemplos.jwt.infrastructure.messaging.listener;

import com.ejemplos.jwt.application.ports.out.EmailNotificationPort;
import com.ejemplos.jwt.infrastructure.messaging.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Listener (Consumidor) que procesa los mensajes de la cola de emails.
 * <p>
 * Este componente actúa como un "Worker" en segundo plano.
 * Si el envío de email falla, la configuración de reintentos (Retry) se encargará,
 * y si falla definitivamente, irá a la Dead Letter Queue (DLQ).
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailListener {

    private final EmailNotificationPort emailService;

    /**
     * Método que se despierta cuando llega un mensaje a la cola.
     *
     * @param emailDTO El mensaje deserializado automáticamente desde JSON.
     */
    @RabbitListener(
            queues = {"${rabbitmq.email.reset.queue}"},
            concurrency = "4"
    )
    public void consumeEmailPasswordReset(EmailRequest emailDTO) {
        log.info("Consuming email password reset for: {}", emailDTO.to());
        // Delegamos la tarea real al adaptador de email (SMTP)
        emailService.sendRecoveryEmail(emailDTO.to(), emailDTO.link());
    }
}
