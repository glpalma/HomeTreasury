package com.glpalma.HomeTreasury.home;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface HomeInviteRepository extends JpaRepository<HomeInvite, Long> {

    Optional<HomeInvite> findByCodeAndUsedFalseAndExpiresAtAfter(String code, Instant now);
}