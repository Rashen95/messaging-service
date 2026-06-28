package ru.privalov.dto;

import java.util.List;
import java.util.UUID;

public record UsersExistsRequest(
        List<UUID> recipientIds
) {
}
