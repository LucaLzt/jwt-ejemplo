package com.ejemplos.jwt.infrastructure.messaging.publisher;

import com.ejemplos.jwt.infrastructure.messaging.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailPublisher {

    @Value("${rabbitmq.email.reset.exchange}")
    private String emailRecoveryPasswordExchange;

    @Value("${rabbitmq.email.reset.routing-key}")
    private String emailRecoveryPasswordRoutingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public void sendEmailPasswordReset(String to, String link) {
        EmailRequest recoveryEmailDTO = new EmailRequest(to, link);
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
