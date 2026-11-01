package com.mushroom.auth.repository;

import com.mushroom.auth.entity.TokenType;
import com.mushroom.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByTokenHashAndTypeAndUsedAtIsNull(String tokenHash, TokenType type);

    @Modifying
    @Query("UPDATE VerificationToken v SET v.usedAt = :now WHERE v.userId = :userId AND v.type = :type AND v.usedAt IS NULL")
    void invalidateExistingTokens(@Param("userId") UUID userId, @Param("type") TokenType type, @Param("now") Instant now);
}
