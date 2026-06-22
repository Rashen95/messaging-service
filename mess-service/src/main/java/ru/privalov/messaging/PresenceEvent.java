package ru.privalov.messaging;

import java.time.Instant;

public record PresenceEvent(
        Long userId,
        String replicaId,
        PresenceStatus status,
        Instant occurredAt
) {
}
