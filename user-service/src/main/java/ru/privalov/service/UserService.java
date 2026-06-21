package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privalov.constant.ErrorPatternConstants;
import ru.privalov.dto.refresh.AccessTokenResponse;
import ru.privalov.dto.login.JwtResponse;
import ru.privalov.dto.login.LoginRequest;
import ru.privalov.dto.refresh.RefreshTokenRequest;
import ru.privalov.dto.registration.UserRegistrationRequest;
import ru.privalov.dto.registration.UserRegistrationResponse;
import ru.privalov.exception.DuplicateUserException;
import ru.privalov.exception.InvalidCredentialsException;
import ru.privalov.mapper.UserMapper;
import ru.privalov.model.User;
import ru.privalov.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public UserRegistrationResponse register(UserRegistrationRequest request) {
        User user = userMapper.toEntity(request, passwordEncoder.encode(request.password()));

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateUserException(ErrorPatternConstants.USERNAME_ALREADY_EXISTS.formatted(user.getUsername()));
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateUserException(ErrorPatternConstants.EMAIL_ALREADY_EXISTS.formatted(user.getEmail()));
        }

        return userMapper.toRegistrationResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(userMapper.normalize(request.username()))
                .orElseThrow(() -> new InvalidCredentialsException(ErrorPatternConstants.INVALID_USERNAME_OR_PASSWORD));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_USERNAME_OR_PASSWORD);
        }

        return jwtService.createTokens(user);
    }

    @Transactional(readOnly = true)
    public AccessTokenResponse refresh(RefreshTokenRequest request) {
        String username = jwtService.extractUsernameFromRefreshToken(request.refreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN));

        return jwtService.createAccessToken(user);
    }
}
