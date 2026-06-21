package ru.privalov.dto.registration;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserRegistrationResponse(
        String username,

        String email,

        String firstName,

        String lastName,

        LocalDate birthDate
) {
}
