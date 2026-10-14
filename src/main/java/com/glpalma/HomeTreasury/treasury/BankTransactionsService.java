package com.glpalma.HomeTreasury.treasury;

import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.home.PluggyItem;
import com.glpalma.HomeTreasury.home.PluggyItemRepository;
import com.glpalma.HomeTreasury.pluggy.PluggyAccount;
import com.glpalma.HomeTreasury.pluggy.PluggyClient;
import com.glpalma.HomeTreasury.pluggy.PluggyPaymentData;
import com.glpalma.HomeTreasury.pluggy.PluggyTransaction;
import com.glpalma.HomeTreasury.pluggy.PluggyTransactionsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reads a Home's BANK-account transactions live from Pluggy. Credit card accounts are skipped on
 * purpose: their transactions are purchases on an invoice, not movements of cash.
 */
@Service
public class BankTransactionsService {

    private static final String BANK_ACCOUNT_TYPE = "BANK";

    /**
     * @param truncated true when Pluggy reported another page for at least one account, i.e.
     *                  {@code transactions} is incomplete. Only the first page is read.
     */
    public record Result(List<BankTransaction> transactions, boolean truncated) {
    }

    private final PluggyClient pluggyClient;
    private final PluggyItemRepository pluggyItems;

    public BankTransactionsService(PluggyClient pluggyClient, PluggyItemRepository pluggyItems) {
        this.pluggyClient = pluggyClient;
        this.pluggyItems = pluggyItems;
    }

    public Result forMonth(Home home, YearMonth month) {
        return forRange(home, month.atDay(1), month.atEndOfMonth());
    }

    /** Both bounds inclusive. */
    public Result forRange(Home home, LocalDate from, LocalDate to) {
        PluggyItem item = pluggyItems.findByHome(home)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No Pluggy item linked to this home"));

        var accountsResponse = pluggyClient.getAccounts(item.getItemId());
        List<PluggyAccount> accounts = accountsResponse == null || accountsResponse.results() == null
                ? List.of()
                : accountsResponse.results();

        List<BankTransaction> all = new ArrayList<>();
        boolean truncated = false;
        for (PluggyAccount account : accounts) {
            if (!BANK_ACCOUNT_TYPE.equals(account.type())) {
                continue;
            }
            PluggyTransactionsResponse response = pluggyClient.getTransactions(account.id(), from, to);
            if (response == null || response.results() == null) {
                continue;
            }
            if (response.next() != null) {
                truncated = true;
            }
            for (PluggyTransaction tx : response.results()) {
                BankTransaction mapped = toBankTransaction(account, tx);
                // Pluggy's from/to filtering is by timestamp; re-check the calendar date ourselves
                // so a transaction on the boundary day can never leak into the neighbouring month.
                if (mapped != null && !mapped.date().isBefore(from) && !mapped.date().isAfter(to)) {
                    all.add(mapped);
                }
            }
        }

        all.sort(Comparator.comparing(BankTransaction::date).reversed()
                .thenComparing(tx -> tx.description() == null ? "" : tx.description()));
        return new Result(List.copyOf(all), truncated);
    }

    private static BankTransaction toBankTransaction(PluggyAccount account, PluggyTransaction tx) {
        if (tx.date() == null || tx.date().length() < 10) {
            return null;
        }
        return new BankTransaction(
                tx.id(),
                account.id(),
                account.name(),
                LocalDate.parse(tx.date().substring(0, 10)),
                tx.description(),
                counterpartName(tx),
                tx.amount(),
                tx.type(),
                tx.status(),
                tx.operationType()
        );
    }

    private static String counterpartName(PluggyTransaction tx) {
        PluggyPaymentData data = tx.paymentData();
        if (data == null) {
            return null;
        }
        var party = "CREDIT".equals(tx.type()) ? data.payer() : data.receiver();
        return party == null ? null : party.name();
    }
}
