package com.glpalma.HomeTreasury.treasury;

import java.math.BigDecimal;

public record DashboardResponse(
        BigDecimal currentBalance,
        BigDecimal idealBalance,
        BigDecimal delta,
        HealthStatus status,
        Growth growth,
        Alarm alarm
) {
    public record Growth(int periodDays, BigDecimal netChange, BigDecimal rate) {
    }

    public record Alarm(boolean underBalance) {
    }
}
