package ru.privalov.messaging;

import java.time.Instant;
import java.util.UUID;

public record StoreMessageCommand(
        UUID messageId,
        UUID senderId,
        UUID recipientId,
        String content,
        Instant sentAt
) {
}
