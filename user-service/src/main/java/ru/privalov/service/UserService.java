package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privalov.dto.login.JwtResponse;
import ru.privalov.dto.login.LoginRequest;
import ru.privalov.dto.registration.UserRegistrationRequest;
import ru.privalov.dto.registration.UserRegistrationResponse;
import ru.privalov.exception.DuplicateUserException;
import ru.privalov.exception.InvalidCredentialsException;
import ru.privalov.model.User;
import ru.privalov.repository.UserRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserRegistrationResponse register(UserRegistrationRequest request) {
        String username = normalize(request.username());
        String email = normalize(request.email());

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException(String.format("Пользователь %s уже зарегистрирован в системе", username));
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException(String.format("Email %s уже используется", email));
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .build();

        try {
            return toResponse(userRepository.save(user));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserException("User is already registered");
        }
    }

    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(normalize(request.username()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return jwtService.createAccessToken(user);
    }

    private UserRegistrationResponse toResponse(User user) {
        return new UserRegistrationResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    private String normalize(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
