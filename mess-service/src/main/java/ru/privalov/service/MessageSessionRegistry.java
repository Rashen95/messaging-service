package ru.privalov.service;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class MessageSessionRegistry {

    private final ConcurrentMap<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(UUID userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void unregister(UUID userId, WebSocketSession session) {
        sessions.remove(userId, session);
    }

    public Optional<WebSocketSession> find(UUID userId) {
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) {
            return Optional.empty();
        }
        return Optional.of(session);
    }
}
