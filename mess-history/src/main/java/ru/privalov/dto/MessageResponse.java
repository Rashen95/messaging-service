package ru.privalov.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        Long senderId,
        Long recipientId,
        String content,
        Instant sentAt
) {
}
