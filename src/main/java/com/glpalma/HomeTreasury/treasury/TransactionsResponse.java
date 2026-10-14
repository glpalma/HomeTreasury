package com.glpalma.HomeTreasury.treasury;

import java.time.YearMonth;
import java.util.List;

public record TransactionsResponse(
        YearMonth month,
        boolean truncated,
        List<BankTransaction> transactions
) {
}
