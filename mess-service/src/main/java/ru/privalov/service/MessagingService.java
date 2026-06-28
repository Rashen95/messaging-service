package ru.privalov.service;

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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService {

    private final RabbitTemplate rabbitTemplate;
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

        Map<UUID, Boolean> existsByUserIds = userServiceClient.existsByUserIds(request.recipientIds());

        Instant sentAt = Instant.now();
        UUID messageId = UUID.randomUUID();

        existsByUserIds.entrySet().stream()
                .peek(entry -> {
                    if (!entry.getValue()) {
                        log.warn("Пользователь с id={} не найден, сообщение доставлено не будет", entry.getKey());
                    }
                })
                .filter(Map.Entry::getValue)
                .forEach(entry -> {
                    rabbitTemplate.convertAndSend(historyExchange, historyRoutingKey, new StoreMessageCommand(
                            messageId,
                            senderId,
                            entry.getKey(),
                            request.content(),
                            sentAt
                    ));

                    ConnectionResponse connection = connectionClient.findConnection(entry.getKey());
                    if (connection == null || !connection.online()) {
                        log.debug("Пользователь с id={} offline. Сообщение сохранено в историю", entry.getKey());
                        return;
                    }

                    Optional<WebSocketSession> clientWSSession = messageSessionRegistry.find(entry.getKey());

                    if (clientWSSession.isPresent()) {
                        try {
                            clientWSSession.get().sendMessage(new TextMessage(request.content()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        rabbitTemplate.convertAndSend(deliveryExchange, connection.replicaId(), new DeliveryCommand(
                                messageId,
                                senderId,
                                entry.getKey(),
                                request.content(),
                                sentAt
                        ));
                    }
                });
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
