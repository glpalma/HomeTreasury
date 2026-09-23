package com.glpalma.HomeTreasury.auth;

import com.glpalma.HomeTreasury.user.AppUser;
import com.glpalma.HomeTreasury.user.AppUserRepository;
import com.glpalma.HomeTreasury.user.HomeMembership;
import com.glpalma.HomeTreasury.user.HomeMembershipRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository users;
    private final HomeMembershipRepository memberships;
    private final JwtService jwtService;
    private final RegistrationService registrationService;

    public AuthController(
            AuthenticationManager authenticationManager,
            AppUserRepository users,
            HomeMembershipRepository memberships,
            JwtService jwtService,
            RegistrationService registrationService
    ) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.memberships = memberships;
        this.jwtService = jwtService;
        this.registrationService = registrationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        AppUser user = users.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        HomeMembership membership = memberships.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        String token = jwtService.createToken(user.getEmail(), membership.getRole().name());
        return new LoginResponse(token, user.getEmail(), membership.getRole().name());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        return registrationService.register(request);
    }
}