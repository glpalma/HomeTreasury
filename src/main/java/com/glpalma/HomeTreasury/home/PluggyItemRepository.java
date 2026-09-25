package com.glpalma.HomeTreasury.home;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PluggyItemRepository extends JpaRepository<PluggyItem, Long> {

    Optional<PluggyItem> findByHome(Home home);
}
