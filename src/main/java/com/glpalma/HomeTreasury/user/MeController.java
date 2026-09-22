package com.glpalma.HomeTreasury.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CurrentMembershipService memberships;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public MeController(
            CurrentMembershipService memberships,
            AppUserRepository users,
            PasswordEncoder passwordEncoder
    ) {
        this.memberships = memberships;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
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

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        AppUser user = memberships.require(authentication).user();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        users.save(user);

    }
}