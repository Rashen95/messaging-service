package ru.privalov.dto.login;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,

        String refreshToken
) {
}
