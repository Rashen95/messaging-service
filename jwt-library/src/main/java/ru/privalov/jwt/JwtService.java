package ru.privalov.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

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

    public JwtTokenPair createTokens(JwtUserPayload userPayload) {
        return new JwtTokenPair(
                createToken(userPayload, accessExpiration, ACCESS_TOKEN_TYPE),
                createToken(userPayload, refreshExpiration, REFRESH_TOKEN_TYPE)
        );
    }

    public String createAccessToken(JwtUserPayload userPayload) {
        return createToken(userPayload, accessExpiration, ACCESS_TOKEN_TYPE);
    }

    public UUID extractUserId(String accessToken) {
        return extractAccessTokenPayload(accessToken).userId();
    }

    public UUID extractUserIdFromRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken);

        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtTokenException("refresh token is required");
        }

        return claims.get(USER_ID_CLAIM, UUID.class);
    }

    public JwtAccessTokenPayload extractAccessTokenPayload(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtTokenException("access token is required");
        }

        String userId = claims.get(USER_ID_CLAIM, String.class);
        if (userId == null || userId.isBlank()) {
            throw new JwtTokenException("userId claim is required");
        }

        return new JwtAccessTokenPayload(
                UUID.fromString(userId),
                claims.getSubject(),
                claims.get(EMAIL_CLAIM, String.class),
                claims.getExpiration().toInstant()
        );
    }

    public JwtRefreshTokenPayload extractRefreshTokenPayload(String refreshToken) {
        Claims claims = parseClaims(refreshToken);

        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtTokenException("refresh token is required");
        }

        return new JwtRefreshTokenPayload(
                claims.getSubject(),
                claims.getExpiration().toInstant()
        );
    }

    private String createToken(JwtUserPayload userPayload, Duration expiration, String tokenType) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);

        return Jwts.builder()
                .subject(userPayload.username())
                .claim(USER_ID_CLAIM, userPayload.userId().toString())
                .claim(EMAIL_CLAIM, userPayload.email())
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
            throw new JwtTokenException("invalid token", e);
        }
    }
}
