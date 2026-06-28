package ru.privalov.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public DirectExchange presenceExchange(@Value("${messaging.rabbit.presence-exchange}") String exchange) {
        return new DirectExchange(exchange);
    }

    @Bean
    public DirectExchange historyExchange(@Value("${messaging.rabbit.history-exchange}") String exchange) {
        return new DirectExchange(exchange);
    }

    @Bean
    public DirectExchange deliveryExchange(@Value("${messaging.rabbit.delivery-exchange}") String exchange) {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue deliveryQueue(@Value("${messaging.rabbit.delivery-queue-prefix}") String queuePrefix,
                               @Value("${messaging.replica-id}") String replicaId) {
        return new Queue(queuePrefix + replicaId, true);
    }

    @Bean
    public Binding deliveryBinding(Queue deliveryQueue,
                                   DirectExchange deliveryExchange,
                                   @Value("${messaging.replica-id}") String replicaId) {
        return BindingBuilder.bind(deliveryQueue).to(deliveryExchange).with(replicaId);
    }

    @Bean
    public MessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
