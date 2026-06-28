package ru.privalov.jwt;

import java.time.Instant;
import java.util.UUID;

public record JwtAccessTokenPayload(
        UUID userId,

        String username,

        String email,

        Instant expiresAt
) {
}
