package com.glpalma.HomeTreasury.user;

import com.glpalma.HomeTreasury.config.OwnerProperties;
import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.home.HomeRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class OwnerInitializer {
    @Bean
    @Order(2)
    ApplicationRunner seedOwner(
            AppUserRepository users,
            HomeMembershipRepository memberships,
            HomeRepository homes,
            OwnerProperties props,
            PasswordEncoder encoder
    ) {
        return args -> {
            if (users.existsByEmail(props.email())) {
                return;
            }

            var homeList = homes.findAll();
            if (homeList.isEmpty()) {
                throw new IllegalStateException("No home row; HomeInitializer should have run first");
            }
            Home home = homeList.getFirst();

            if (memberships.existsByHome(home)) {
                return;
            }

            AppUser owner = users.save(new AppUser(props.email(), encoder.encode(props.password())));
            memberships.save(new HomeMembership(owner, home, Role.OWNER));
        };
    }
}