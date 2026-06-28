package ru.privalov.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.privalov.dto.ConnectionResponse;
import ru.privalov.dto.IncomingMessageRequest;
import ru.privalov.messaging.DeliveryCommand;
import ru.privalov.messaging.StoreMessageCommand;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ConnectionClient connectionClient;
    private final UserServiceClient userServiceClient;
    private final MessageSessionRegistry messageSessionRegistry;

    @Value("${messaging.rabbit.history-exchange}")
    private String historyExchange;

    @Value("${messaging.rabbit.history-routing-key}")
    private String historyRoutingKey;

    @Value("${messaging.rabbit.delivery-exchange}")
    private String deliveryExchange;

    public void processIncoming(UUID senderId, IncomingMessageRequest request) {
        validate(senderId, request);

        Map<UUID, Boolean> existsByUserIds = Optional
                .ofNullable(userServiceClient.existsByUserIds(request.recipientIds()))
                .orElse(Collections.emptyMap());

        Instant sentAt = Instant.now();

        existsByUserIds.forEach((recipientId, exists) -> {
            UUID messageId = UUID.randomUUID();

            if (!exists) {
                log.warn("User with id={} not found, message will not be delivered", recipientId);
                return;
            }

            rabbitTemplate.convertAndSend(historyExchange, historyRoutingKey, new StoreMessageCommand(
                    messageId,
                    senderId,
                    recipientId,
                    request.content(),
                    sentAt
            ));

            ConnectionResponse connection = connectionClient.findConnection(recipientId);
            if (connection == null || !connection.online()) {
                log.debug("User with id={} is offline. Message was stored in history", recipientId);
                return;
            }

            DeliveryCommand deliveryCommand = new DeliveryCommand(
                    messageId,
                    senderId,
                    recipientId,
                    request.content(),
                    sentAt
            );
            Optional<WebSocketSession> clientWSSession = messageSessionRegistry.find(recipientId);

            if (clientWSSession.isPresent()) {
                log.debug("sendLocal");
                sendLocal(clientWSSession.get(), deliveryCommand);
            } else {
                log.debug("sendRemote to {}", connection.replicaId());
                rabbitTemplate.convertAndSend(deliveryExchange, connection.replicaId(), deliveryCommand);
            }
        });
    }

    private void sendLocal(WebSocketSession session, DeliveryCommand command) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(command)));
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize message {} for user {}", command.messageId(), command.recipientId(), exception);
        } catch (IOException exception) {
            log.warn("Failed to deliver message {} to user {}", command.messageId(), command.recipientId(), exception);
        }
    }

    private void validate(UUID senderId, IncomingMessageRequest request) {
        if (senderId == null) {
            throw new IllegalArgumentException("senderId is required");
        }
        if (request == null || CollectionUtils.isEmpty(request.recipientIds())) {
            throw new IllegalArgumentException("recipientId is required");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
    }
}
