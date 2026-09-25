package com.glpalma.HomeTreasury.treasury;

import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.home.PluggyItem;
import com.glpalma.HomeTreasury.home.PluggyItemRepository;
import com.glpalma.HomeTreasury.pluggy.PluggyAccount;
import com.glpalma.HomeTreasury.pluggy.PluggyClient;
import com.glpalma.HomeTreasury.pluggy.PluggyTransaction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class TreasuryService {

    private static final BigDecimal EPSILON = new BigDecimal("0.01");

    private final PluggyClient pluggyClient;
    private final PluggyItemRepository pluggyItems;

    public TreasuryService(PluggyClient pluggyClient, PluggyItemRepository pluggyItems) {
        this.pluggyClient = pluggyClient;
        this.pluggyItems = pluggyItems;
    }

    public DashboardResponse dashboard(Home home, int periodDays) {
        PluggyItem item = pluggyItems.findByHome(home)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No Pluggy item linked to this home"));

        List<PluggyAccount> accounts = accountsOrEmpty(item.getItemId());
        BigDecimal current = accounts.stream()
                .map(a -> a.balance() == null ? BigDecimal.ZERO : a.balance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ideal = home.getIdealBalance() == null ? BigDecimal.ZERO : home.getIdealBalance();
        BigDecimal delta = current.subtract(ideal);
        HealthStatus status = delta.signum() < 0 ? HealthStatus.BELOW
                : delta.signum() == 0 ? HealthStatus.AT
                : HealthStatus.ABOVE;

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(periodDays);
        BigDecimal netChange = BigDecimal.ZERO;
        for (PluggyAccount account : accounts) {
            for (PluggyTransaction tx : transactionsOrEmpty(account.id(), from, to)) {
                if (tx.amount() != null) {
                    netChange = netChange.add(tx.amount());
                }
            }
        }
        BigDecimal opening = current.subtract(netChange).abs().max(EPSILON);
        BigDecimal rate = netChange.divide(opening, 4, RoundingMode.HALF_UP);

        List<DashboardResponse.AccountSummary> accountSummaries = accounts.stream()
                .map(a -> new DashboardResponse.AccountSummary(
                        a.id(), a.name(), a.type(),
                        a.balance() == null ? BigDecimal.ZERO : a.balance(),
                        a.currencyCode()
                ))
                .toList();

        return new DashboardResponse(
                current,
                ideal,
                delta,
                status,
                new DashboardResponse.Growth(periodDays, netChange, rate),
                new DashboardResponse.Alarm(status == HealthStatus.BELOW),
                accountSummaries
        );
    }

    private List<PluggyAccount> accountsOrEmpty(String itemId) {
        var results = pluggyClient.getAccounts(itemId).results();
        return results == null ? List.of() : results;
    }

    private List<PluggyTransaction> transactionsOrEmpty(String accountId, LocalDate from, LocalDate to) {
        var results = pluggyClient.getTransactions(accountId, from, to).results();
        return results == null ? List.of() : results;
    }
}
