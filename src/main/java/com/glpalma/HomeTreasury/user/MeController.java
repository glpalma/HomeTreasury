package com.glpalma.HomeTreasury.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CurrentMembershipService memberships;

    public MeController(CurrentMembershipService memberships) {
        this.memberships = memberships;
    }

    @GetMapping
    public MeResponse me(Authentication authentication) {
        var snapshot = memberships.require(authentication);
        return new MeResponse(
                snapshot.user().getEmail(),
                snapshot.membership().getRole().name(),
                new MeResponse.HomeSummary(snapshot.home().getId(), snapshot.home().getName())
        );
    }
}