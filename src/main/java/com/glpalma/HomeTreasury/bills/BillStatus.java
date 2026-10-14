package com.glpalma.HomeTreasury.bills;

public enum BillStatus {
    /** A matching DEBIT exists in the month (or the OWNER marked it paid). */
    PAID,
    /** Not paid, and the pay-by date is today or within the next 5 days. */
    DUE_SOON,
    /** Not paid, and the pay-by date is already behind us. */
    OVERDUE,
    /** Not paid, and the pay-by date is more than 5 days away. */
    UPCOMING
}
