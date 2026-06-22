package ru.privalov.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;
import ru.privalov.dto.IncomingMessageRequest;
import ru.privalov.messaging.PresenceEvent;
import ru.privalov.messaging.PresenceStatus;
import ru.privalov.service.MessageSessionRegistry;
import ru.privalov.service.MessagingService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final String USER_ID_ATTRIBUTE = "userId";

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
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session)
                .orElseThrow(() -> new IllegalArgumentException("Query parameter userId is required"));

        session.getAttributes().put(USER_ID_ATTRIBUTE, userId);
        messageSessionRegistry.register(userId, session);
        publishPresence(userId, PresenceStatus.CONNECTED);
        log.debug("User {} connected to replica {}", userId, replicaId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Long senderId = (Long) session.getAttributes().get(USER_ID_ATTRIBUTE);
            IncomingMessageRequest request = objectMapper.readValue(message.getPayload(), IncomingMessageRequest.class);
            messagingService.processIncoming(senderId, request);
        } catch (Exception exception) {
            log.warn("Failed to process websocket message: {}", message.getPayload(), exception);
            sendError(session, exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get(USER_ID_ATTRIBUTE);
        if (userId == null) {
            return;
        }

        messageSessionRegistry.unregister(userId, session);
        publishPresence(userId, PresenceStatus.DISCONNECTED);
        log.debug("User {} disconnected from replica {}", userId, replicaId);
    }

    private Optional<Long> extractUserId(WebSocketSession session) {
        if (session.getUri() == null) {
            return Optional.empty();
        }
        String userId = UriComponentsBuilder.fromUri(session.getUri()).build()
                .getQueryParams()
                .getFirst(USER_ID_ATTRIBUTE);
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(userId));
    }

    private void publishPresence(Long userId, PresenceStatus status) {
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
