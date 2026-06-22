package ru.privalov.dto;

public record IncomingMessageRequest(
        Long recipientId,
        String content
) {
}
