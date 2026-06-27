package ru.privalov.dto;

import java.util.UUID;

public record ConnectionResponse(
        UUID userId,
        boolean online,
        String replicaId
) {
}
