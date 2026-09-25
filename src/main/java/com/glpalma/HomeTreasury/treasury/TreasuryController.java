package com.glpalma.HomeTreasury.treasury;

import com.glpalma.HomeTreasury.user.CurrentMembershipService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/treasury")
public class TreasuryController {

    private final TreasuryService treasuryService;
    private final CurrentMembershipService memberships;

    public TreasuryController(TreasuryService treasuryService, CurrentMembershipService memberships) {
        this.treasuryService = treasuryService;
        this.memberships = memberships;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(
            @RequestParam(defaultValue = "30") int periodDays,
            Authentication authentication
    ) {
        var home = memberships.require(authentication).home();
        return treasuryService.dashboard(home, periodDays);
    }
}
