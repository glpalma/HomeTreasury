package com.glpalma.HomeTreasury.treasury;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal currentBalance,
        BigDecimal idealBalance,
        BigDecimal creditCardDue,
        BigDecimal delta,
        HealthStatus status,
        Growth growth,
        Alarm alarm,
        List<AccountSummary> accounts
) {
    public record Growth(int periodDays, BigDecimal netChange, BigDecimal rate) {
    }

    /**
     * {@code underBalance} means: after paying this cycle's {@code creditCardDue} in full, this
     * Home's {@code currentBalance} would dip below {@code idealBalance} (its emergency reserve).
     */
    public record Alarm(boolean underBalance) {
    }

    /**
     * One account backing this Home's Pluggy connection. {@code type} is Pluggy's {@code BANK}
     * or {@code CREDIT}. Only {@code BANK} accounts' balances are summed into
     * {@code currentBalance} — a {@code CREDIT} account's {@code balance} is the amount owed on
     * its open invoice (a liability), not spendable cash, so it is listed here for visibility but
     * excluded from the aggregate.
     */
    public record AccountSummary(String id, String name, String type, BigDecimal balance, String currencyCode) {
    }
}
