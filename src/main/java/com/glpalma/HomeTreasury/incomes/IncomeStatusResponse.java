package com.glpalma.HomeTreasury.incomes;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * @param expectedTotal Sum of the expected amounts of every income active in the month.
 * @param receivedTotal Sum of the amounts actually found (absolute values) for the matched ones.
 */
public record IncomeStatusResponse(
        YearMonth month,
        boolean transactionsTruncated,
        BigDecimal expectedTotal,
        BigDecimal receivedTotal,
        List<IncomeMonthStatus> incomes
) {
}
