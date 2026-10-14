package com.glpalma.HomeTreasury.incomes;

import com.glpalma.HomeTreasury.bills.BillMatch;
import com.glpalma.HomeTreasury.bills.BillStatusCalculator;
import com.glpalma.HomeTreasury.bills.HomeClock;
import com.glpalma.HomeTreasury.bills.TransactionMatcher;
import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.treasury.BankTransaction;
import com.glpalma.HomeTreasury.treasury.BankTransactionsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class IncomeStatusService {

    private final ExpectedIncomeRepository incomes;
    private final BankTransactionsService bankTransactions;

    public IncomeStatusService(ExpectedIncomeRepository incomes, BankTransactionsService bankTransactions) {
        this.incomes = incomes;
        this.bankTransactions = bankTransactions;
    }

    public IncomeStatusResponse statusFor(Home home, YearMonth month) {
        List<ExpectedIncome> active = incomes.findByHomeOrderByExpectedDayAscNameAsc(home).stream()
                .filter(income -> isActiveIn(income, month))
                .toList();
        if (active.isEmpty()) {
            return new IncomeStatusResponse(month, false, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
        }

        BankTransactionsService.Result result = bankTransactions.forMonth(home, month);
        LocalDate today = HomeClock.today();

        List<IncomeMonthStatus> statuses = active.stream()
                .map(income -> statusOf(income, month, result.transactions(), today))
                .toList();

        BigDecimal expectedTotal = active.stream()
                .map(ExpectedIncome::getExpectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receivedTotal = statuses.stream()
                .filter(status -> status.match() != null)
                .map(status -> status.match().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new IncomeStatusResponse(month, result.truncated(), expectedTotal, receivedTotal, statuses);
    }

    private static IncomeMonthStatus statusOf(
            ExpectedIncome income,
            YearMonth month,
            List<BankTransaction> transactions,
            LocalDate today
    ) {
        LocalDate expectedBy = BillStatusCalculator.payByDate(month, income.getExpectedDay());
        List<BankTransaction> matching = TransactionMatcher
                .matches(income.getKeyword(), TransactionMatcher.Direction.CREDIT, transactions);
        BillMatch match = matching.isEmpty() ? null : BillMatch.of(matching, income.getExpectedAmount());

        IncomeStatus status;
        if (match != null) {
            status = IncomeStatus.RECEIVED;
        } else if (today.isAfter(expectedBy)) {
            // Same rule as bills: arriving on the expected day itself is still on time.
            status = IncomeStatus.MISSING;
        } else {
            status = IncomeStatus.EXPECTED;
        }

        return new IncomeMonthStatus(
                income.getId(),
                income.getName(),
                income.getExpectedAmount(),
                income.getExpectedDay(),
                expectedBy,
                status,
                match
        );
    }

    /** An income applies from its start month through its end month, both inclusive (no end = forever). */
    private static boolean isActiveIn(ExpectedIncome income, YearMonth month) {
        if (month.isBefore(income.getStartMonth())) {
            return false;
        }
        return income.getEndMonth() == null || !month.isAfter(income.getEndMonth());
    }
}
