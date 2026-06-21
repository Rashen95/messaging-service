package ru.privalov.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.privalov.constant.ErrorPatternConstants;
import ru.privalov.dto.login.JwtResponse;
import ru.privalov.dto.refresh.AccessTokenResponse;
import ru.privalov.exception.InvalidCredentialsException;
import ru.privalov.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String USER_ID_CLAIM = "userId";
    private static final String EMAIL_CLAIM = "email";

    private final SecretKey secretKey;
    private final Duration accessExpiration;
    private final Duration refreshExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") Duration accessExpiration,
            @Value("${jwt.refresh-expiration}") Duration refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public JwtResponse createTokens(User user) {
        return JwtResponse.builder()
                .accessToken(createToken(user, accessExpiration, ACCESS_TOKEN_TYPE))
                .refreshToken(createToken(user, refreshExpiration, REFRESH_TOKEN_TYPE))
                .build();
    }

    public AccessTokenResponse createAccessToken(User user) {
        return AccessTokenResponse.builder()
                .accessToken(createToken(user, accessExpiration, ACCESS_TOKEN_TYPE))
                .build();
    }

    public RefreshTokenPayload extractRefreshTokenPayload(String refreshToken) {
        Claims claims = parseClaims(refreshToken);

        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN);
        }

        return RefreshTokenPayload.builder()
                .username(claims.getSubject())
                .expiresAt(claims.getExpiration().toInstant())
                .build();
    }

    private String createToken(User user, Duration expiration, String tokenType) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(USER_ID_CLAIM, user.getId().toString())
                .claim(EMAIL_CLAIM, user.getEmail())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new InvalidCredentialsException(ErrorPatternConstants.INVALID_REFRESH_TOKEN);
        }
    }

    @Builder
    public record RefreshTokenPayload(
            String username,

            Instant expiresAt
    ) {
    }
}
