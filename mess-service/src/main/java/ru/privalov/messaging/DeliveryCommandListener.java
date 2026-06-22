package ru.privalov.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @RabbitListener(queues = "#{deliveryQueue.name}")
    public void handle(DeliveryCommand command) {
        messageSessionRegistry.find(command.recipientId()).ifPresent(session -> {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(command)));
            } catch (Exception exception) {
                log.warn("Failed to deliver message {} to user {}", command.messageId(), command.recipientId(), exception);
            }
        });
    }
}
