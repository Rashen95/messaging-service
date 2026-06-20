package ru.privalov.dto;

public record JwtResponse(
        String accessToken,

        String tokenType,

        long expiresIn
) {
}
