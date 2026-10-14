package com.glpalma.HomeTreasury.incomes;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ExpectedIncomeResponse(
        Long id,
        String name,
        BigDecimal expectedAmount,
        int expectedDay,
        String keyword,
        YearMonth startMonth,
        YearMonth endMonth
) {
    public static ExpectedIncomeResponse from(ExpectedIncome income) {
        return new ExpectedIncomeResponse(
                income.getId(),
                income.getName(),
                income.getExpectedAmount(),
                income.getExpectedDay(),
                income.getKeyword(),
                income.getStartMonth(),
                income.getEndMonth()
        );
    }
}
