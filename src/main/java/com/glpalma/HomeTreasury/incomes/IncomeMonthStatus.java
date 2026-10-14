package com.glpalma.HomeTreasury.incomes;

import com.glpalma.HomeTreasury.bills.BillMatch;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One expected income's state in one month. {@code match} is null when nothing was found. */
public record IncomeMonthStatus(
        Long incomeId,
        String name,
        BigDecimal expectedAmount,
        int expectedDay,
        LocalDate expectedBy,
        IncomeStatus status,
        BillMatch match
) {
}
