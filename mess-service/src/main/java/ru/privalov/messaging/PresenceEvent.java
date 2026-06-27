package ru.privalov.messaging;

import java.time.Instant;
import java.util.UUID;

public record PresenceEvent(
        UUID userId,
        String replicaId,
        PresenceStatus status,
        Instant occurredAt
) {
}
