package ru.privalov;

import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privalov.dto.JwtResponse;
import ru.privalov.dto.LoginRequest;
import ru.privalov.dto.UserRegistrationRequest;
import ru.privalov.dto.UserResponse;
import ru.privalov.exception.DuplicateUserException;
import ru.privalov.exception.InvalidCredentialsException;
import ru.privalov.model.User;
import ru.privalov.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        String username = normalize(request.username());
        String email = normalize(request.email());

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException("Username is already registered");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("Email is already registered");
        }

        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.password())
        );

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

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
