package com.glpalma.HomeTreasury.bills;

import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.treasury.BankTransaction;
import com.glpalma.HomeTreasury.treasury.BankTransactionsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BillStatusService {

    private final BillRepository bills;
    private final BillPaymentMarkRepository marks;
    private final BankTransactionsService bankTransactions;

    public BillStatusService(
            BillRepository bills,
            BillPaymentMarkRepository marks,
            BankTransactionsService bankTransactions
    ) {
        this.bills = bills;
        this.marks = marks;
        this.bankTransactions = bankTransactions;
    }

    public BillStatusResponse statusFor(Home home, YearMonth month) {
        List<Bill> active = bills.findByHomeOrderByDueDayAscNameAsc(home).stream()
                .filter(bill -> isActiveIn(bill, month))
                .toList();
        if (active.isEmpty()) {
            return new BillStatusResponse(month, false, List.of());
        }

        Set<Long> markedBillIds = marks.findByBillInAndMonth(active, month).stream()
                .map(mark -> mark.getBill().getId())
                .collect(Collectors.toSet());

        BankTransactionsService.Result result = bankTransactions.forMonth(home, month);
        LocalDate today = HomeClock.today();

        List<BillMonthStatus> statuses = active.stream()
                .map(bill -> statusOf(bill, month, result.transactions(), markedBillIds.contains(bill.getId()), today))
                .toList();
        return new BillStatusResponse(month, result.truncated(), statuses);
    }

    private static BillMonthStatus statusOf(
            Bill bill,
            YearMonth month,
            List<BankTransaction> transactions,
            boolean manuallyMarked,
            LocalDate today
    ) {
        LocalDate payBy = BillStatusCalculator.payByDate(month, bill.getDueDay());
        List<BankTransaction> matching = TransactionMatcher
                .matches(bill.getKeyword(), TransactionMatcher.Direction.DEBIT, transactions);
        BillMatch match = matching.isEmpty() ? null : BillMatch.of(matching, bill.getExpectedAmount());
        BillStatus status = BillStatusCalculator.statusOf(payBy, match != null || manuallyMarked, today);
        return new BillMonthStatus(
                bill.getId(),
                bill.getName(),
                bill.getExpectedAmount(),
                bill.getDueDay(),
                payBy,
                status,
                match,
                manuallyMarked
        );
    }

    /** A bill applies from its start month through its end month, both inclusive (no end = forever). */
    static boolean isActiveIn(Bill bill, YearMonth month) {
        if (month.isBefore(bill.getStartMonth())) {
            return false;
        }
        return bill.getEndMonth() == null || !month.isAfter(bill.getEndMonth());
    }
}
