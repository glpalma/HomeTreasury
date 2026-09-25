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

    /**
     * Pluggy's Account.type for deposit accounts (checking/savings), where {@code balance} is
     * spendable cash. The other value, {@code CREDIT}, means {@code balance} is the amount
     * currently owed on the card's open invoice — a liability, not cash — so it must never be
     * summed into {@code currentBalance}/growth, but it does reduce {@code delta} (see below).
     */
    private static final String BANK_ACCOUNT_TYPE = "BANK";
    private static final String CREDIT_ACCOUNT_TYPE = "CREDIT";

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
        List<PluggyAccount> bankAccounts = accounts.stream()
                .filter(a -> BANK_ACCOUNT_TYPE.equals(a.type()))
                .toList();
        List<PluggyAccount> creditAccounts = accounts.stream()
                .filter(a -> CREDIT_ACCOUNT_TYPE.equals(a.type()))
                .toList();

        BigDecimal current = sumBalances(bankAccounts);

        // Pluggy's CREDIT balance is already the current invoice's total due (Brazilian cards
        // bill each month's installment portions into one open invoice, so this is exactly "what
        // I owe this cycle", not the card's full outstanding balance across future installments).
        BigDecimal creditCardDue = sumBalances(creditAccounts);

        BigDecimal ideal = home.getIdealBalance() == null ? BigDecimal.ZERO : home.getIdealBalance();

        // "Cash health" here means: after keeping the emergency reserve (idealBalance) untouched,
        // is there still enough cash to pay this cycle's credit card bill in full? That's the bar
        // this Home needs to clear to be considered ABOVE/AT rather than BELOW.
        BigDecimal delta = current.subtract(ideal).subtract(creditCardDue);
        HealthStatus status = delta.signum() < 0 ? HealthStatus.BELOW
                : delta.signum() == 0 ? HealthStatus.AT
                : HealthStatus.ABOVE;

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(periodDays);
        BigDecimal netChange = BigDecimal.ZERO;
        for (PluggyAccount account : bankAccounts) {
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
                creditCardDue,
                delta,
                status,
                new DashboardResponse.Growth(periodDays, netChange, rate),
                new DashboardResponse.Alarm(status == HealthStatus.BELOW),
                accountSummaries
        );
    }

    private static BigDecimal sumBalances(List<PluggyAccount> accounts) {
        return accounts.stream()
                .map(a -> a.balance() == null ? BigDecimal.ZERO : a.balance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
