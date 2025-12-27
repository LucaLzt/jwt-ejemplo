package com.ejemplos.jwt.infrastructure.messaging.publisher;

import com.ejemplos.jwt.infrastructure.messaging.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPublisher {

    @Value("${rabbitmq.email.reset.exchange}")
    private String emailRecoveryPasswordExchange;

    @Value("${rabbitmq.email.reset.routing-key}")
    private String emailRecoveryPasswordRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public void sendEmailPasswordReset(String to, String link) {
        EmailRequest recoveryEmailDTO = new EmailRequest(to, link);
        var id = UUID.randomUUID().toString();

        log.info("Sending email password reset to: {}", to);

        rabbitTemplate.convertAndSend(
                emailRecoveryPasswordExchange,
                emailRecoveryPasswordRoutingKey,
                recoveryEmailDTO,
                new CorrelationData(id)
        );
    }
}
