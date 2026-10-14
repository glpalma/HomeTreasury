package com.glpalma.HomeTreasury.bills;

import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.treasury.BankTransaction;
import com.glpalma.HomeTreasury.treasury.BankTransactionsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BillPreviewService {

    private static final int MAX_MONTHS = 6;

    private final BankTransactionsService bankTransactions;

    public BillPreviewService(BankTransactionsService bankTransactions) {
        this.bankTransactions = bankTransactions;
    }

    public BillPreviewResponse preview(Home home, String keyword, int months) {
        int window = Math.max(1, Math.min(months, MAX_MONTHS));
        YearMonth current = HomeClock.currentMonth();
        LocalDate from = current.minusMonths(window - 1L).atDay(1);
        LocalDate to = HomeClock.today();

        BankTransactionsService.Result result = bankTransactions.forRange(home, from, to);
        List<BankTransaction> matched = new ArrayList<>(
                TransactionMatcher.matches(keyword, TransactionMatcher.Direction.DEBIT, result.transactions()));
        Collections.reverse(matched); // newest first for display

        List<BillPreviewMatch> matches = matched.stream()
                .map(tx -> new BillPreviewMatch(
                        tx.date(),
                        tx.description(),
                        tx.amount() == null ? BigDecimal.ZERO : tx.amount().abs()))
                .toList();

        BigDecimal average = null;
        if (!matches.isEmpty()) {
            BigDecimal sum = matches.stream()
                    .map(BillPreviewMatch::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            average = sum.divide(BigDecimal.valueOf(matches.size()), 2, RoundingMode.HALF_UP);
        }
        return new BillPreviewResponse(keyword, window, result.truncated(), matches, average);
    }
}
