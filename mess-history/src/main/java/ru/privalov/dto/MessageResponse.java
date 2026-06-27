package ru.privalov.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID senderId,
        UUID recipientId,
        String content,
        Instant sentAt
) {
}
