package ru.privalov.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import ru.privalov.service.MessageSessionRegistry;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCommandListener {

    private final MessageSessionRegistry messageSessionRegistry;
    private final ObjectMapper objectMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${messaging.rabbit.delivery-queue-prefix}${messaging.replica-id}", durable = "false"),
            exchange = @Exchange(value = "${messaging.rabbit.delivery-exchange}", type = ExchangeTypes.DIRECT),
            key = "${messaging.replica-id}"
    ))
    public void handle(DeliveryCommand command) {
        messageSessionRegistry.find(command.recipientId()).ifPresentOrElse(session -> {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(command)));
            } catch (Exception exception) {
                log.warn("Failed to deliver message {} to user {}", command.messageId(), command.recipientId(), exception);
            }
        }, () -> log.warn("No local websocket session for user {}, message {}", command.recipientId(), command.messageId()));
    }
}
