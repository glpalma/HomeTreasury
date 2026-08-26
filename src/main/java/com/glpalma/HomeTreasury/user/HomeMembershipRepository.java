package com.glpalma.HomeTreasury.user;

import com.glpalma.HomeTreasury.home.Home;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomeMembershipRepository extends JpaRepository<HomeMembership, Long> {

    boolean existsByHome(Home home);

    Optional<HomeMembership> findByUser(AppUser user);
}
