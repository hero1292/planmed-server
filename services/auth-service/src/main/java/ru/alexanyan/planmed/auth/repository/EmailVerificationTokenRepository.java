package ru.alexanyan.planmed.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexanyan.planmed.auth.model.EmailVerificationToken;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
        update EmailVerificationToken t
        set t.usedAt = :usedAt
        where t.userId = :userId and t.usedAt is null
    """)
    int invalidateAllForUser(@Param("userId") UUID userId,
                             @Param("usedAt") OffsetDateTime usedAt);

    @Query("""
        select t from EmailVerificationToken t
        where t.userId = :userId and t.usedAt is null
        order by t.expiresAt desc
    """)
    List<EmailVerificationToken> findActiveByUser(@Param("userId") UUID userId);
}
