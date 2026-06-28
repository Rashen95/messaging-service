package ru.privalov.jwt;

public record JwtTokenPair(
        String accessToken,

        String refreshToken
) {
}
