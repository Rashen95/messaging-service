package ru.privalov.dto;

import java.util.UUID;

public record IncomingMessageRequest(
        UUID recipientId,
        String content
) {
}
