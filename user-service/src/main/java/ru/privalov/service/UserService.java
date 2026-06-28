package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privalov.constant.ErrorPatternConstants;
import ru.privalov.dto.login.LoginRequest;
import ru.privalov.dto.login.LoginResponse;
import ru.privalov.dto.refresh.RefreshTokenRequest;
import ru.privalov.dto.refresh.RefreshTokenResponse;
import ru.privalov.dto.registration.UserRegistrationRequest;
import ru.privalov.dto.registration.UserRegistrationResponse;
import ru.privalov.exception.DuplicateUserException;
import ru.privalov.exception.InvalidCredentialsException;
import ru.privalov.jwt.JwtRefreshTokenPayload;
import ru.privalov.jwt.JwtService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(userMapper.normalize(request.username()))
                .orElseThrow(() -> new InvalidCredentialsException(ErrorPatternConstants.INVALID_USERNAME_OR_PASSWORD));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_USERNAME_OR_PASSWORD);
        }

        JwtTokenPair tokenPair = jwtService.createTokens(toJwtUserPayload(user));
        LoginResponse response = LoginResponse.builder()
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .build();
        JwtRefreshTokenPayload payload = jwtService.extractRefreshTokenPayload(response.refreshToken());

        refreshTokenRepository.save(
                new RefreshToken(user, hashToken(response.refreshToken()), payload.expiresAt())
        );

        return response;
    }

    @Transactional(readOnly = true)
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        JwtRefreshTokenPayload payload = jwtService.extractRefreshTokenPayload(request.refreshToken());
        RefreshToken refreshToken = findActiveRefreshToken(request.refreshToken());

        if (!refreshToken.getUser().getUsername().equals(payload.username())) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN);
        }

        return RefreshTokenResponse.builder()
                .accessToken(jwtService.createAccessToken(toJwtUserPayload(refreshToken.getUser())))
                .build();
    }

    @Transactional
    public void logoutUser(RefreshTokenRequest request) {
        UUID userId = jwtService.extractUserIdFromRefreshToken(request.refreshToken());

        if (userId == null) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN);
        }

        List<RefreshToken> allByUserIdAndRevokedAtIsNullAndIsActive = refreshTokenRepository
                .findAllByUserIdAndRevokedAtIsNullAndIsActive(userId, Instant.now());

        allByUserIdAndRevokedAtIsNullAndIsActive.forEach(RefreshToken::revoke);
    }

    @Transactional(readOnly = true)
    public Map<UUID, Boolean> usersExists(List<UUID> userIds) {
        return userIds.stream()
                .collect(Collectors.toMap(Function.identity(), userRepository::existsById));
    }

    private RefreshToken findActiveRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hashToken(token))
                .orElseThrow(() -> new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN));

        if (!refreshToken.isActive()) {
            throw new InvalidCredentialsException(ErrorPatternConstants.EXPIRED_REFRESH_TOKEN);
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

    private JwtUserPayload toJwtUserPayload(User user) {
        return new JwtUserPayload(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
