package com.glpalma.HomeTreasury.bills;

import java.time.YearMonth;
import java.util.List;

public record BillStatusResponse(
        YearMonth month,
        /** True when Pluggy had more transactions than we read, so a bill may look unpaid by mistake. */
        boolean transactionsTruncated,
        List<BillMonthStatus> bills
) {
}
