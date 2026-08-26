package com.glpalma.HomeTreasury.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService  implements UserDetailsService {

    private final AppUserRepository users;
    private final HomeMembershipRepository memberships;

    public AppUserDetailsService(AppUserRepository users, HomeMembershipRepository memberships) {
        this.users = users;
        this.memberships = memberships;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = users.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        HomeMembership membership = memberships.findByUser(user)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(membership.getRole().name())
                .build();
    }
}