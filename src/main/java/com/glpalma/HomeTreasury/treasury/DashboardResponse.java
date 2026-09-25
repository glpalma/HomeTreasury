package com.glpalma.HomeTreasury.treasury;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal currentBalance,
        BigDecimal idealBalance,
        BigDecimal delta,
        HealthStatus status,
        Growth growth,
        Alarm alarm,
        List<AccountSummary> accounts
) {
    public record Growth(int periodDays, BigDecimal netChange, BigDecimal rate) {
    }

    public record Alarm(boolean underBalance) {
    }

    /**
     * One bank account contributing to {@code currentBalance}. {@code currentBalance} is always
     * the sum of every entry here — this list exists so the SPA can show which account(s) the
     * aggregate figure came from instead of presenting it as an opaque single number.
     */
    public record AccountSummary(String id, String name, String type, BigDecimal balance, String currencyCode) {
    }
}
