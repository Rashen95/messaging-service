package ru.privalov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.privalov.model.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    @Query("""
            select token
            from RefreshToken token
            where token.user.id = :userId
            and token.revokedAt is null
            and token.expiresAt > :now
            """)
    List<RefreshToken> findAllByUserIdAndRevokedAtIsNullAndIsActive(UUID userId, Instant now);

    @Modifying
    @Query("""
            delete from RefreshToken token
            where token.expiresAt < :expiredBefore
               or token.revokedAt < :revokedBefore
            """)
    int deleteExpiredOrRevokedBefore(Instant expiredBefore, Instant revokedBefore);
}
