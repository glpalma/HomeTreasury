package com.glpalma.HomeTreasury.incomes;

import com.glpalma.HomeTreasury.bills.HomeClock;
import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.user.CurrentMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final CurrentMembershipService memberships;
    private final ExpectedIncomeRepository incomes;
    private final IncomeStatusService incomeStatus;

    public IncomeController(
            CurrentMembershipService memberships,
            ExpectedIncomeRepository incomes,
            IncomeStatusService incomeStatus
    ) {
        this.memberships = memberships;
        this.incomes = incomes;
        this.incomeStatus = incomeStatus;
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpectedIncomeResponse create(
            @Valid @RequestBody CreateExpectedIncomeRequest request,
            Authentication authentication
    ) {
        Home home = memberships.require(authentication).home();
        YearMonth startMonth = request.startMonth() == null ? HomeClock.currentMonth() : request.startMonth();
        ExpectedIncome saved = incomes.save(new ExpectedIncome(
                home,
                request.name().trim(),
                request.expectedAmount(),
                request.expectedDay(),
                request.keyword().trim(),
                startMonth
        ));
        return ExpectedIncomeResponse.from(saved);
    }

    @GetMapping
    public List<ExpectedIncomeResponse> list(Authentication authentication) {
        Home home = memberships.require(authentication).home();
        return incomes.findByHomeOrderByExpectedDayAscNameAsc(home).stream()
                .map(ExpectedIncomeResponse::from)
                .toList();
    }

    @GetMapping("/status")
    public IncomeStatusResponse status(
            @RequestParam YearMonth month,
            Authentication authentication
    ) {
        Home home = memberships.require(authentication).home();
        return incomeStatus.statusFor(home, month);
    }
}
