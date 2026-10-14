package com.glpalma.HomeTreasury.bills;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BillResponse(
        Long id,
        String name,
        BigDecimal expectedAmount,
        int dueDay,
        String keyword,
        YearMonth startMonth,
        YearMonth endMonth
) {
    public static BillResponse from(Bill bill) {
        return new BillResponse(
                bill.getId(),
                bill.getName(),
                bill.getExpectedAmount(),
                bill.getDueDay(),
                bill.getKeyword(),
                bill.getStartMonth(),
                bill.getEndMonth()
        );
    }
}
