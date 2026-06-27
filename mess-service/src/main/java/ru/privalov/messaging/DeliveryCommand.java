package ru.privalov.messaging;

import java.time.Instant;
import java.util.UUID;

public record DeliveryCommand(
        UUID messageId,
        UUID senderId,
        UUID recipientId,
        String content,
        Instant sentAt
) {
}
