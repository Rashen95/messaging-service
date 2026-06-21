package ru.privalov.dto.refresh;

import lombok.Builder;

@Builder
public record AccessTokenResponse(
        String accessToken
) {
}
