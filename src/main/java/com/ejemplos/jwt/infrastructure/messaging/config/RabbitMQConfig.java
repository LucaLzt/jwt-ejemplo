package com.ejemplos.jwt.infrastructure.messaging.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuración central de RabbitMQ para toda la aplicación.
 * <p>
 * Esta clase define:
 * - El MessageConverter (JSON)
 * - El RabbitTemplate (publisher confirms + returns)
 * - La fábrica de listeners con retry + backoff + DLX
 * <p>
 * Nada de lógica de colas particulares va aquí.
 * Eso se define en clases de configuración por módulo (ej: EmailMessagingConfig).
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    @Value("${rabbitmq.dlx.name}")
    private String dlxName;

    @Bean
    public TopicExchange appDlx() {
        return new TopicExchange(dlxName);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory, MessageConverter messageConverter) {

        // Habilitamos "publisher confirms" (importante para confiabilidad)
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);

        // Habilitamos "publisher returns" (detecta errores de enrutamiento)
        connectionFactory.setPublisherReturns(true);

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        // mandatory = true → si un mensaje no encuentra cola, vuelve al publisher
        rabbitTemplate.setMandatory(true);

        // CALLBACK: confirmación de publicación en el exchange
        rabbitTemplate.setConfirmCallback(((correlationData, ack, cause) -> {
            String id = (correlationData != null ? correlationData.getId() : "null");
            if (ack) {
                log.info("Message with ID {} confirmed by the broker.", id);
            } else {
                log.warn("Message with ID {} NOT confirmed by the broker. Cause: {}", id, cause);
            }
        }));

        // CALLBACK: mensaje que llegó al exchange pero NO a ninguna queue
        rabbitTemplate.setReturnsCallback(returnedMessage -> log.error(
                "Returned Message - Exchange: {}, RoutingKey, {}, Reply: {}",
                returnedMessage.getExchange(),
                returnedMessage.getRoutingKey(),
                returnedMessage.getReplyText()
        ));

        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        var factory = new SimpleRabbitListenerContainerFactory();

        // Conexión usada por los consumidores
        factory.setConnectionFactory(connectionFactory);

        // Conversión automática de JSON
        factory.setMessageConverter(messageConverter);

        // Retry con backoff exponencial
        var advice = RetryInterceptorBuilder
                .stateless()
                .maxAttempts(5)                 // 5 intentos en total
                .backOffOptions(
                        1000,                   // 1 segundo
                        2.0,                    // multiplicador (2x)
                        10000                   // máximo 10 segundos
                )
                // Cuando se agotan los intentos → reject sin requeue → DLX
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();

        factory.setAdviceChain(advice);

        return factory;
    }
}
