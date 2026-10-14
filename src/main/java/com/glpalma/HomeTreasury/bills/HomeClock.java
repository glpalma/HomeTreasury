package com.glpalma.HomeTreasury.bills;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * The one place that answers "what day is it?" for bill logic. The household lives in Brazil, so
 * "today" must be Brazilian today regardless of the server's own time zone.
 */
public final class HomeClock {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private HomeClock() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static YearMonth currentMonth() {
        return YearMonth.from(today());
    }
}
