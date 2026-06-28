package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privalov.constant.ErrorPatternConstants;
import ru.privalov.dto.login.JwtResponse;
import ru.privalov.dto.login.LoginRequest;
import ru.privalov.dto.refresh.AccessTokenResponse;
import ru.privalov.dto.refresh.RefreshTokenRequest;
import ru.privalov.dto.registration.UserRegistrationRequest;
import ru.privalov.dto.registration.UserRegistrationResponse;
import ru.privalov.exception.DuplicateUserException;
import ru.privalov.exception.InvalidCredentialsException;
import ru.privalov.jwt.JwtRefreshTokenPayload;
import ru.privalov.jwt.JwtService;
import ru.privalov.jwt.JwtTokenException;
import ru.privalov.jwt.JwtTokenPair;
import ru.privalov.jwt.JwtUserPayload;
import ru.privalov.mapper.UserMapper;
import ru.privalov.model.RefreshToken;
import ru.privalov.model.User;
import ru.privalov.repository.RefreshTokenRepository;
import ru.privalov.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
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

    @Transactional
    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(userMapper.normalize(request.username()))
                .orElseThrow(() -> new InvalidCredentialsException(ErrorPatternConstants.INVALID_USERNAME_OR_PASSWORD));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_USERNAME_OR_PASSWORD);
        }

        JwtTokenPair tokenPair = jwtService.createTokens(toJwtUserPayload(user));
        JwtResponse response = JwtResponse.builder()
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .build();
        JwtRefreshTokenPayload payload = extractRefreshTokenPayload(response.refreshToken());
        saveRefreshToken(user, response.refreshToken(), payload.expiresAt());

        return response;
    }

    @Transactional(readOnly = true)
    public AccessTokenResponse refresh(RefreshTokenRequest request) {
        JwtRefreshTokenPayload payload = extractRefreshTokenPayload(request.refreshToken());
        RefreshToken refreshToken = findActiveRefreshToken(request.refreshToken());

        if (!refreshToken.getUser().getUsername().equals(payload.username())) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN);
        }

        return AccessTokenResponse.builder()
                .accessToken(jwtService.createAccessToken(toJwtUserPayload(refreshToken.getUser())))
                .build();
    }

    @Transactional(readOnly = true)
    public Boolean userExists(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = findActiveRefreshToken(request.refreshToken());
        refreshToken.revoke();
    }

    private void saveRefreshToken(User user, String token, Instant expiresAt) {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .tokenHash(hashToken(token))
                        .expiresAt(expiresAt)
                        .createdAt(Instant.now())
                        .build()
        );
    }

    private RefreshToken findActiveRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hashToken(token))
                .orElseThrow(() -> new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN));

        if (!refreshToken.isActive()) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN);
        }

        return refreshToken;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private JwtRefreshTokenPayload extractRefreshTokenPayload(String refreshToken) {
        try {
            return jwtService.extractRefreshTokenPayload(refreshToken);
        } catch (JwtTokenException exception) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN);
        }
    }

    private JwtUserPayload toJwtUserPayload(User user) {
        return new JwtUserPayload(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
