package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.privalov.dto.ConnectionResponse;
import ru.privalov.dto.IncomingMessageRequest;
import ru.privalov.messaging.DeliveryCommand;
import ru.privalov.messaging.StoreMessageCommand;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionClient connectionClient;

    @Value("${messaging.rabbit.history-exchange}")
    private String historyExchange;

    @Value("${messaging.rabbit.history-routing-key}")
    private String historyRoutingKey;

    @Value("${messaging.rabbit.delivery-exchange}")
    private String deliveryExchange;

    public void processIncoming(Long senderId, IncomingMessageRequest request) {
        validate(senderId, request);

        Instant sentAt = Instant.now();
        UUID messageId = UUID.randomUUID();

        rabbitTemplate.convertAndSend(historyExchange, historyRoutingKey, new StoreMessageCommand(
                messageId,
                senderId,
                request.recipientId(),
                request.content(),
                sentAt
        ));

        ConnectionResponse connection = connectionClient.findConnection(request.recipientId());
        if (connection == null || !connection.online()) {
            return;
        }

        rabbitTemplate.convertAndSend(deliveryExchange, connection.replicaId(), new DeliveryCommand(
                messageId,
                senderId,
                request.recipientId(),
                request.content(),
                sentAt
        ));
    }

    private void validate(Long senderId, IncomingMessageRequest request) {
        if (senderId == null) {
            throw new IllegalArgumentException("senderId is required");
        }
        if (request == null || request.recipientId() == null) {
            throw new IllegalArgumentException("recipientId is required");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
    }
}
