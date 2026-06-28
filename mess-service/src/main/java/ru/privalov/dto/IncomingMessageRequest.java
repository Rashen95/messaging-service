package ru.privalov.dto;

import java.util.List;
import java.util.UUID;

public record IncomingMessageRequest(
        List<UUID> recipientIds,
        String content
) {
}
