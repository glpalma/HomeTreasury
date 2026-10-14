package com.glpalma.HomeTreasury.bills;

import java.time.LocalDate;
import java.time.YearMonth;

/** The date rules for bill status. Pure: pass "today" in, get a status out. */
public final class BillStatusCalculator {

    /** How many days before the pay-by date a bill starts to count as "due soon". */
    static final int DUE_SOON_DAYS = 5;

    private BillStatusCalculator() {
    }

    /** The bill's pay-by day inside {@code month}; day 31 in a 30-day month becomes the 30th. */
    public static LocalDate payByDate(YearMonth month, int dueDay) {
        return month.atDay(Math.min(dueDay, month.lengthOfMonth()));
    }

    public static BillStatus statusOf(LocalDate payBy, boolean paid, LocalDate today) {
        if (paid) {
            return BillStatus.PAID;
        }
        // Paying on the pay-by day itself is on time, so overdue starts the day after.
        if (today.isAfter(payBy)) {
            return BillStatus.OVERDUE;
        }
        if (!payBy.isAfter(today.plusDays(DUE_SOON_DAYS))) {
            return BillStatus.DUE_SOON;
        }
        return BillStatus.UPCOMING;
    }
}
