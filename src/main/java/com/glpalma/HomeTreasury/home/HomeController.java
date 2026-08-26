package com.glpalma.HomeTreasury.home;

import com.glpalma.HomeTreasury.user.AppUser;
import com.glpalma.HomeTreasury.user.AppUserRepository;
import com.glpalma.HomeTreasury.user.HomeMembership;
import com.glpalma.HomeTreasury.user.HomeMembershipRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/home")
public class HomeController {
    private final AppUserRepository users;
    private final HomeMembershipRepository memberships;
    private final HomeRepository homes;
    public HomeController(
            AppUserRepository users,
            HomeMembershipRepository memberships,
            HomeRepository homes
    ) {
        this.users = users;
        this.memberships = memberships;
        this.homes = homes;
    }
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/idealBalance")
    public void setIdealBalance(
            @RequestBody BalanceRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        HomeMembership membership = memberships.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        Home home = membership.getHome();
        home.setIdealBalance(request.idealBalance());
        homes.save(home);
    }
}