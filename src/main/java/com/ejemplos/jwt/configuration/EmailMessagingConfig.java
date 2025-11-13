package com.ejemplos.jwt.configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración específica del módulo de Email Reset Password.
 *
 * Esta clase define:
 *  - La queue principal donde se encolan los emails de recuperación
 *  - La DLQ donde terminan mensajes fallidos
 *  - El exchange normal del módulo
 *  - Los bindings correspondientes
 *  - El uso del DLX global definido en RabbitInfraConfig
 *
 * Toda la lógica de infraestructura (RabbitTemplate, ListenerFactory, DLX global, etc.)
 * está ubicada en RabbitInfraConfig, por separación de responsabilidades.
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
     *
     * Lo inyectamos usando @Qualifier porque hay varios TopicExchange definidos
     * en la aplicación, y queremos específicamente el exchange declarado como:
     *
     *   @Bean public TopicExchange appDlx()
     *
     * Este DLX es compartido por todos los módulos del sistema.
     */
    private final TopicExchange dlx;

    public EmailMessagingConfig(@Qualifier("appDlx") TopicExchange dlx) {
        this.dlx = dlx;
    }

    /**
     * Queue principal del módulo.
     *
     * Configuraciones importantes:
     *  - durable(true): persiste tras reinicios del broker
     *  - x-dead-letter-exchange: DLX global
     *  - x-dead-letter-routing-key: routing key hacia la DLQ del módulo
     *
     * Esto asegura que los mensajes fallidos NO se pierdan, sino que terminen
     * en la DLQ correspondiente para su posterior inspección.
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
     * Dead Letter Queue (DLQ) del módulo.
     *
     * Se usa para almacenar mensajes que fallaron todos los reintentos.
     * Esta cola NO tiene DLX ni reintentos adicionales.
     */
    @Bean
    public Queue emailResetDlq() {
        return QueueBuilder
                .durable(emailRecoveryPasswordDlq)
                .build();
    }

    /**
     * Exchange principal de este módulo.
     *
     * Los productores envían a este exchange usando la routing key definida
     * en emailRecoveryPasswordRoutingKey.
     */
    @Bean
    public TopicExchange emailRecoveryExchange() {
        return new TopicExchange(emailRecoveryPasswordExchange);
    }

    /**
     * Binding entre la queue principal y el exchange del módulo.
     *
     * Esta regla indica:
     *   exchange: email.ex
     *   routing-key: email.reset-password
     *   destino: queue email.reset-password.q
     *
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
