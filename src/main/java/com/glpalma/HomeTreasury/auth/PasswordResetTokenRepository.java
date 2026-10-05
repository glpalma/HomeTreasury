package com.glpalma.HomeTreasury.auth;

import com.glpalma.HomeTreasury.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalseAndExpiresAtAfter(String token, Instant now);

    List<PasswordResetToken> findAllByUserAndUsedFalse(AppUser user);
}
