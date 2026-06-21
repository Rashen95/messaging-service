package ru.privalov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.privalov.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    @Modifying
    @Query("""
            delete from RefreshToken token
            where token.expiresAt < :expiredBefore
               or token.revokedAt < :revokedBefore
            """)
    int deleteExpiredOrRevokedBefore(Instant expiredBefore, Instant revokedBefore);
}
