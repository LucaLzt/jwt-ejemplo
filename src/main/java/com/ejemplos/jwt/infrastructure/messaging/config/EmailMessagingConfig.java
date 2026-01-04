package com.ejemplos.jwt.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la topología del módulo de Emails.
 * <p>
 * Define la estructura física en RabbitMQ:
 * - Queue principal (con redirección a DLX en caso de fallo).
 * - Dead Letter Queue (DLQ) para inspección manual.
 * - Bindings para conectar el Exchange con la Queue.
 * </p>
 */
@Configuration
public class EmailMessagingConfig {

    @Value("${rabbitmq.email.reset.queue}")
    private String emailRecoveryPasswordQueue;

    @Value("${rabbitmq.email.reset.exchange}")
    private String emailRecoveryPasswordExchange;

    @Value("${rabbitmq.email.reset.routing-key}")
    private String emailRecoveryPasswordRoutingKey;

    @Value("${rabbitmq.email.reset.dlq}")
    private String emailRecoveryPasswordDlq;

    @Value("${rabbitmq.email.reset.dlq-routing-key}")
    private String emailRecoveryPasswordDlqRoutingKey;

    /**
     * Dead Letter Exchange (DLX) global.
     * <p>
     * Lo inyectamos usando @Qualifier porque hay varios TopicExchange definidos
     * en la aplicación, y queremos específicamente el exchange declarado como:
     *
     * @Bean public TopicExchange appDlx()
     * <p>
     * Este DLX es compartido por todos los módulos del sistema.
     */
    private final TopicExchange dlx;

    public EmailMessagingConfig(@Qualifier("appDlx") TopicExchange dlx) {
        this.dlx = dlx;
    }

    /**
     * Cola principal.
     * Configurada con argumentos "x-dead-letter" para que RabbitMQ sepa
     * a dónde enviar el mensaje si el consumidor lo rechaza definitivamente.
     */
    @Bean
    public Queue emailResetQueue() {
        return QueueBuilder
                .durable(emailRecoveryPasswordQueue)
                .withArgument("x-dead-letter-exchange", dlx.getName())
                .withArgument("x-dead-letter-routing-key", emailRecoveryPasswordDlqRoutingKey)
                .build();
    }

    /**
     * Cola de mensajes muertos (DLQ).
     * Aquí terminan los mensajes "tóxicos" para que no bloqueen el sistema.
     */
    @Bean
    public Queue emailResetDlq() {
        return QueueBuilder
                .durable(emailRecoveryPasswordDlq)
                .build();
    }

    /**
     * Exchange principal de este módulo.
     * <p>
     * Los productores envían a este exchange usando la routing key definida
     * en emailRecoveryPasswordRoutingKey.
     */
    @Bean
    public TopicExchange emailRecoveryExchange() {
        return new TopicExchange(emailRecoveryPasswordExchange);
    }

    /**
     * Binding entre la queue principal y el exchange del módulo.
     * <p>
     * Esta regla indica:
     * exchange: email.ex
     * routing-key: email.reset-password
     * destino: queue email.reset-password.q
     * <p>
     * Es decir, todo mensaje publicado con esa routing key llega a la queue.
     */
    @Bean
    public Binding emailRecoveryBinding() {
        return BindingBuilder
                .bind(emailResetQueue())
                .to(emailRecoveryExchange())
                .with(emailRecoveryPasswordRoutingKey);
    }

}
