package com.glpalma.HomeTreasury.bills;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One bill's state in one month. {@code match} is null when no transaction was found;
 * {@code manuallyMarked} is true when the OWNER marked it paid by hand.
 */
public record BillMonthStatus(
        Long billId,
        String name,
        BigDecimal expectedAmount,
        int dueDay,
        LocalDate payBy,
        BillStatus status,
        BillMatch match,
        boolean manuallyMarked
) {
}
