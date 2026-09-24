package com.glpalma.HomeTreasury.home;

import com.glpalma.HomeTreasury.user.CurrentMembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CurrentMembershipService memberships;
    private final HomeRepository homes;
    private final HomeInviteRepository homeInvites;

    public HomeController(
            CurrentMembershipService memberships,
            HomeRepository homes,
            HomeInviteRepository homeInvites
    ) {
        this.memberships = memberships;
        this.homes = homes;
        this.homeInvites = homeInvites;
    }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/idealBalance")
    public void setIdealBalance(
            @RequestBody BalanceRequest request,
            Authentication authentication
    ) {
        Home home = memberships.require(authentication).home();
        home.setIdealBalance(request.idealBalance());
        homes.save(home);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(Authentication authentication) {
        Home home = memberships.require(authentication).home();
        String code = generateCode();
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
        homeInvites.save(new HomeInvite(home, code, expiresAt));
        return new InviteResponse(code, expiresAt);
    }

    private static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }
}