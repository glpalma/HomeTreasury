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
     * {@code currentBalance} — a {@code CREDIT} account's raw {@code balance} can reflect the
     * card's total used limit (including future installment portions) rather than one invoice,
     * so it is excluded from every aggregate.
     * <p>
     * For {@code CREDIT} accounts, {@code billDue}/{@code billDueDate} are resolved from Pluggy's
     * {@code GET /bills} (the invoice with the closest upcoming due date) and are what
     * {@code creditCardDue} is actually summed from — {@code balance} is kept here only so the
     * two can be compared while this resolution logic is verified against real data. Both are
     * {@code null} for {@code BANK} accounts, and {@code billDue} falls back to {@code balance}
     * when the institution doesn't support the Bills product (see {@code PluggyClient.getBills}).
     */
    public record AccountSummary(
            String id, String name, String type, BigDecimal balance, String currencyCode,
            BigDecimal billDue, String billDueDate
    ) {
    }
}
