package com.glpalma.HomeTreasury.treasury;

import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.home.PluggyItem;
import com.glpalma.HomeTreasury.home.PluggyItemRepository;
import com.glpalma.HomeTreasury.pluggy.PluggyAccount;
import com.glpalma.HomeTreasury.pluggy.PluggyBill;
import com.glpalma.HomeTreasury.pluggy.PluggyBillPayment;
import com.glpalma.HomeTreasury.pluggy.PluggyClient;
import com.glpalma.HomeTreasury.pluggy.PluggyTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TreasuryService {

    private static final Logger log = LoggerFactory.getLogger(TreasuryService.class);

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

        // account.balance() for a CREDIT account can reflect the card's total used limit
        // (including future installment portions) rather than one invoice, so "what I owe this
        // cycle" is resolved per-account from Pluggy's Bills product instead — see dueInfoFor.
        Map<String, DueInfo> dueByAccountId = new HashMap<>();
        for (PluggyAccount account : creditAccounts) {
            dueByAccountId.put(account.id(), dueInfoFor(account));
        }
        BigDecimal creditCardDue = dueByAccountId.values().stream()
                .map(DueInfo::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
                .map(a -> {
                    DueInfo due = dueByAccountId.get(a.id());
                    return new DashboardResponse.AccountSummary(
                            a.id(), a.name(), a.type(),
                            a.balance() == null ? BigDecimal.ZERO : a.balance(),
                            a.currencyCode(),
                            due == null ? null : due.amount(),
                            due == null ? null : due.dueDate()
                    );
                })
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

    /**
     * Resolves "what's owed on the next invoice" for one CREDIT account: the closest-upcoming,
     * not-yet-paid bill. Falls back to the account's raw {@code balance} if the institution
     * doesn't return Bills data at all (unsupported connector), every returned bill is missing a
     * {@code dueDate}, or every bill is already settled — which happens for real between a bill's
     * {@code dueDate} and the next {@code billClosingDate}: Pluggy does not expose a not-yet-closed
     * invoice at all, so the "most recent" bill in that window is just the last one that closed,
     * already paid off. Without the paid check below that stale, settled bill would otherwise be
     * mistaken for the pending one.
     */
    private DueInfo dueInfoFor(PluggyAccount account) {
        List<PluggyBill> bills;
        try {
            var results = pluggyClient.getBills(account.id()).results();
            bills = results == null ? List.of() : results;
        } catch (RestClientException ex) {
            log.warn("Could not fetch bills for credit account {}: {}", account.id(), ex.getMessage());
            bills = List.of();
        }

        List<PluggyBill> withDueDate = bills.stream().filter(b -> b.dueDate() != null).toList();
        if (withDueDate.isEmpty()) {
            log.info("No usable bills for credit account {}; falling back to raw balance", account.id());
            return new DueInfo(account.balance() == null ? BigDecimal.ZERO : account.balance(), null);
        }

        LocalDate today = LocalDate.now();
        Optional<PluggyBill> chosen = withDueDate.stream()
                .filter(b -> !billDueDate(b).isBefore(today))
                .filter(b -> !isFullyPaid(b))
                .min(Comparator.comparing(TreasuryService::billDueDate));

        if (chosen.isEmpty()) {
            // Nothing upcoming and unpaid. Prefer an overdue-but-unpaid bill (still a real debt,
            // most urgent one) over an already-settled bill.
            chosen = withDueDate.stream()
                    .filter(b -> !isFullyPaid(b))
                    .max(Comparator.comparing(TreasuryService::billDueDate));
        }

        if (chosen.isEmpty()) {
            // Every returned bill is fully paid — we're between this cycle's due date and the
            // next closing, and Pluggy hasn't produced that invoice yet. Raw balance is the best
            // available signal for "what's owed right now" until it does.
            log.info("Every bill for credit account {} is already paid; falling back to raw balance",
                    account.id());
            return new DueInfo(account.balance() == null ? BigDecimal.ZERO : account.balance(), null);
        }

        BigDecimal amount = chosen.get().totalAmount() == null ? BigDecimal.ZERO : chosen.get().totalAmount();
        return new DueInfo(amount, chosen.get().dueDate());
    }

    private static LocalDate billDueDate(PluggyBill bill) {
        return LocalDate.parse(bill.dueDate().substring(0, 10));
    }

    /**
     * Whether a bill's {@code payments} already cover its {@code totalAmount} (within a cent, to
     * absorb rounding on either side) — i.e. this invoice is settled and shouldn't be reported as
     * currently due.
     */
    private static boolean isFullyPaid(PluggyBill bill) {
        if (bill.totalAmount() == null) {
            return false;
        }
        List<PluggyBillPayment> payments = bill.payments();
        BigDecimal paid = payments == null ? BigDecimal.ZERO : payments.stream()
                .map(p -> p.amount() == null ? BigDecimal.ZERO : p.amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return paid.compareTo(bill.totalAmount().subtract(EPSILON)) >= 0;
    }

    private record DueInfo(BigDecimal amount, String dueDate) {
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
