package ru.privalov.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
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
    public Queue presenceQueue(@Value("${messaging.rabbit.presence-queue}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    public Binding presenceBinding(Queue presenceQueue,
                                   DirectExchange presenceExchange,
                                   @Value("${messaging.rabbit.presence-routing-key}") String routingKey) {
        return BindingBuilder.bind(presenceQueue).to(presenceExchange).with(routingKey);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter jacksonJsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJsonMessageConverter);
        return factory;
    }
}
