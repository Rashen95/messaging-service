package ru.privalov.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.privalov.dto.IncomingMessageRequest;
import ru.privalov.messaging.PresenceEvent;
import ru.privalov.messaging.PresenceStatus;
import ru.privalov.service.MessageSessionRegistry;
import ru.privalov.service.MessagingService;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USER_ID_HEADER = "X-User-Id";

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final MessageSessionRegistry messageSessionRegistry;
    private final MessagingService messagingService;

    @Value("${messaging.replica-id}")
    private String replicaId;

    @Value("${messaging.rabbit.presence-exchange}")
    private String presenceExchange;

    @Value("${messaging.rabbit.presence-routing-key}")
    private String presenceRoutingKey;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        UUID userId = extractUserId(session);

        messageSessionRegistry.register(userId, session);
        publishPresence(userId, PresenceStatus.CONNECTED);
        log.debug("User {} connected to replica {}", userId, replicaId);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        try {
            UUID senderId = extractUserId(session);
            IncomingMessageRequest request = objectMapper.readValue(message.getPayload(), IncomingMessageRequest.class);
            messagingService.processIncoming(senderId, request);
        } catch (Exception exception) {
            log.warn("Failed to process websocket message: {}", message.getPayload(), exception);
            sendError(session, exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        UUID userId = (UUID) session.getAttributes().get(USER_ID_ATTRIBUTE);
        if (userId == null) {
            return;
        }

        messageSessionRegistry.unregister(userId, session);
        publishPresence(userId, PresenceStatus.DISCONNECTED);
        log.debug("User {} disconnected from replica {}", userId, replicaId);
    }

    private UUID extractUserId(WebSocketSession session) {
        String userId = session.getHandshakeHeaders().getFirst(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Authenticated user header is required");
        }
        return UUID.fromString(userId);
    }

    private void publishPresence(UUID userId, PresenceStatus status) {
        rabbitTemplate.convertAndSend(
                presenceExchange,
                presenceRoutingKey,
                new PresenceEvent(userId, replicaId, status, Instant.now())
        );
    }

    private void sendError(WebSocketSession session, String message) throws Exception {
        if (!session.isOpen()) {
            return;
        }
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                "type", "ERROR",
                "message", message
        ))));
    }
}
