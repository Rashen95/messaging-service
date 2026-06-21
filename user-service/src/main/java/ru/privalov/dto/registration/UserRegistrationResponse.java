package ru.privalov.dto.registration;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserRegistrationResponse(
        UUID id,

        String username,

        String email
) {
}
