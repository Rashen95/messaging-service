package ru.privalov.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserRegistrationRequest(
        @NotBlank
        @Size(max = 50)
        String username,

        @Email
        @NotBlank
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        String firstName,

        String lastName,

        LocalDate birthDate
) {
}
