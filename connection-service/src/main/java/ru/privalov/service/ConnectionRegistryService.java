package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.privalov.dto.ConnectionResponse;
import ru.privalov.messaging.PresenceEvent;
import ru.privalov.messaging.PresenceStatus;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ConnectionRegistryService {

    private static final String KEY_PATTERN = "connection:user:%d";

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${messaging.connections.ttl}")
    private Duration connectionTtl;

    public void apply(PresenceEvent event) {
        String key = key(event.userId());

        if (event.status() == PresenceStatus.CONNECTED) {
            redisTemplate.opsForValue().set(key, event.replicaId(), connectionTtl);
            return;
        }

        String currentReplicaId = redisTemplate.opsForValue().get(key);
        if (event.replicaId().equals(currentReplicaId)) {
            redisTemplate.delete(key);
        }
    }

    public ConnectionResponse find(Long userId) {
        String replicaId = redisTemplate.opsForValue().get(key(userId));
        return new ConnectionResponse(userId, replicaId != null, replicaId);
    }

    private String key(Long userId) {
        return KEY_PATTERN.formatted(userId);
    }
}
