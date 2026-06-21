package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.privalov.repository.RefreshTokenRepository;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupService {

    private static final Duration TOKEN_RETENTION = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public int cleanupTokens() {
        Instant cleanupBefore = Instant.now().minus(TOKEN_RETENTION);
        return refreshTokenRepository.deleteExpiredOrRevokedBefore(cleanupBefore, cleanupBefore);
    }
}
