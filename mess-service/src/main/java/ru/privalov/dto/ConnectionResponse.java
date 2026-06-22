package ru.privalov.dto;

public record ConnectionResponse(
        Long userId,
        boolean online,
        String replicaId
) {
}
