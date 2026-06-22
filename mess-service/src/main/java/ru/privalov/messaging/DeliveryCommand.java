package ru.privalov.messaging;

import java.time.Instant;
import java.util.UUID;

public record DeliveryCommand(
        UUID messageId,
        Long senderId,
        Long recipientId,
        String content,
        Instant sentAt
) {
}
