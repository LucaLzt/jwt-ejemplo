package com.ejemplos.jwt.infrastructure.messaging.listener;

import com.ejemplos.jwt.application.ports.out.EmailNotificationPort;
import com.ejemplos.jwt.infrastructure.messaging.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailListener {

    private final EmailNotificationPort emailService;

    @RabbitListener(
            queues = {"${rabbitmq.email.reset.queue}"},
            concurrency = "4"
    )
    public void consumeEmailPasswordReset(EmailRequest emailDTO) {
        log.info("Consuming email password reset for: {}", emailDTO.to());
        emailService.sendRecoveryEmail(emailDTO.to(), emailDTO.link());
    }
}
