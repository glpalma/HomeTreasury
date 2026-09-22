package com.glpalma.HomeTreasury.user;

import com.glpalma.HomeTreasury.home.Home;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentMembershipService {

    public record Snapshot(AppUser user, HomeMembership membership, Home home) {
    }

    private final AppUserRepository users;
    private final HomeMembershipRepository memberships;

    public CurrentMembershipService(AppUserRepository users, HomeMembershipRepository memberships) {
        this.users = users;
        this.memberships = memberships;
    }

    public Snapshot require(Authentication authentication) {
        String email = authentication.getName();
        AppUser user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        HomeMembership membership = memberships.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        return new Snapshot(user, membership, membership.getHome());
    }
}