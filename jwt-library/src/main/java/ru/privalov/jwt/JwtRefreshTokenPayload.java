package ru.privalov.jwt;

import java.time.Instant;

public record JwtRefreshTokenPayload(
        String username,

        Instant expiresAt
) {
}
