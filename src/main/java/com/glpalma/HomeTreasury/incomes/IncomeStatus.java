package com.glpalma.HomeTreasury.incomes;

public enum IncomeStatus {
    /** A matching CREDIT exists in the month. */
    RECEIVED,
    /** Nothing found yet, and the expected day has not passed. */
    EXPECTED,
    /** Nothing found, and the expected day is already behind us. */
    MISSING
}
