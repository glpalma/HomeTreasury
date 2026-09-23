package com.glpalma.HomeTreasury.auth;

import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.home.HomeInviteRepository;
import com.glpalma.HomeTreasury.home.HomeRepository;
import com.glpalma.HomeTreasury.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class RegistrationService {

    private final AppUserRepository users;
    private final HomeMembershipRepository memberships;
    private final HomeRepository homes;
    private final HomeInviteRepository homeInvites;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegistrationService(
            AppUserRepository users,
            HomeMembershipRepository memberships,
            HomeRepository homes,
            HomeInviteRepository homeInvites,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.users = users;
        this.memberships = memberships;
        this.homes = homes;
        this.homeInvites = homeInvites;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        AppUser user = users.save(
                new AppUser(request.email(), passwordEncoder.encode(request.password())));
        HomeMembership membership;

        if (request.inviteCode() != null && !request.inviteCode().isBlank()) {
            var invite = homeInvites
                    .findByCodeAndUsedFalseAndExpiresAtAfter(request.inviteCode(), Instant.now())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Invalid or expired invite code"));
            invite.markUsed();
            membership = memberships.save(new HomeMembership(user, invite.getHome(), Role.VIEWER));
        } else if (request.homeName() != null && !request.homeName().isBlank()) {
            Home home = homes.save(new Home(request.homeName()));
            membership = memberships.save(new HomeMembership(user, home, Role.OWNER));
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Either homeName or inviteCode must be provided");
        }

        String token = jwtService.createToken(user.getEmail(), membership.getRole().name());
        return new LoginResponse(token, user.getEmail(), membership.getRole().name());
    }
}