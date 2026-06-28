package ru.privalov.dto;

import java.util.List;
import java.util.UUID;

public record ExistsRequest(
        List<UUID> recipientIds
) {
}
