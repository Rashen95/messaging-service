package ru.privalov.jwt;

import java.util.UUID;

public record JwtUserPayload(
        UUID userId,

        String username,

        String email
) {
}
