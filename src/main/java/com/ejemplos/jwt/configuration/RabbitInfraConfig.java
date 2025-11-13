package com.ejemplos.jwt.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *
 * Esta clase define:
 *  - El MessageConverter (JSON)
 *  - El RabbitTemplate (publisher confirms + returns)
 *  - La fábrica de listeners con retry + backoff + DLX
 *
 * Nada de lógica de colas particulares va aquí.
 * Eso se define en clases de configuración por módulo (ej: EmailMessagingConfig).
 */
@Configuration
public class RabbitInfraConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitInfraConfig.class);

    @Value("${rabbitmq.dlx.name}")
    private String dlxName;


    /**
     * Dead Letter Exchange global (DLX).
     *
     * Se define como TopicExchange para permitir routing keys dinámicas.
     *
     * Ejemplo:
     *   - Mensajes fallidos del módulo email → email.reset-password.dlq
     *   - Mensajes fallidos del módulo órdenes → order.created.dlq
     *
     * Cada módulo declara su propia DLQ, pero comparten este DLX.
     *
     */
    @Bean
    public TopicExchange appDlx() {
        return new TopicExchange(dlxName);
    }

    /**
     * Converter a JSON para mensajes.
     *
     * RabbitMQ solo maneja bytes, y este converter permite:
     *  - Serializar objetos Java → JSON (al enviar)
     *  - Deserializar JSON → objeto Java (al recibir)
     *
     * Spring AMQP usa este converter en los productores y consumers.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configuración del RabbitTemplate.
     *
     * Este bean se usa para enviar mensajes a RabbitMQ.
     * Se activa:
     *  - Publisher Confirms → el broker confirma si recibió el mensaje.
     *  - Publisher Returns → se notifica si un mensaje NO puede enrutar.
     *  - Mandatory = true → obliga al broker a devolver mensajes no enrutable.
     */
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
                LOGGER.info(String.format("Mensaje con ID %s confirmado por el broker", id));
            } else {
                LOGGER.warn(String.format("Mensaje con ID %s NO confirmado por el broker. Causa: %s", id, cause));
            }
        }));

        // CALLBACK: mensaje que llegó al exchange pero NO a ninguna queue
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            LOGGER.error(String.format("Mensaje devuelto - Exchange: %s, RoutingKey, %s, Reply: %s"),
                    returnedMessage.getExchange(),
                    returnedMessage.getRoutingKey(),
                    returnedMessage.getReplyText());
        });

        return rabbitTemplate;
    }

    /**
     * Fábrica de contenedores para los @RabbitListener.
     *
     * Aquí configuramos:
     *  - ConnectionFactory
     *  - Converter
     *  - Retry automático de mensajes
     *  - Backoff exponencial (1s, 2s, 4s, 8s...)
     *  - Recoverer que hace "reject" (sin requeue) → RabbitMQ envía a DLX
     *
     * Esta fábrica se usa para todos los listeners, salvo que alguno
     * declare explícitamente otro containerFactory.
     */
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
