package com.ejemplos.jwt.infrastructure.messaging.listener;

import com.ejemplos.jwt.application.ports.out.EmailNotificationPort;
import com.ejemplos.jwt.infrastructure.messaging.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailListener.class);

    private final EmailNotificationPort emailService;

    @RabbitListener(
            queues =  {"${rabbitmq.email.reset.queue}"},
            concurrency = "4"
    )
    public void consumeEmailPasswordReset(EmailRequest emailDTO) {
        LOGGER.info(String.format("Consuming email password reset for: %s", emailDTO.to()));
        emailService.sendRecoveryEmail(emailDTO.to(), emailDTO.link());
    }
}
