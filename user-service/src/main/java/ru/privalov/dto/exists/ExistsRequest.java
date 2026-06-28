package ru.privalov.dto.exists;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ExistsRequest(
        @NotNull
        List<UUID> recipientIds
) {
}
