package com.glpalma.HomeTreasury.bills;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.YearMonth;

/** Full replacement of the editable fields; unlike creation, {@code startMonth} is required. */
public record UpdateBillRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0.01") BigDecimal expectedAmount,
        @Min(1) @Max(31) int dueDay,
        @NotBlank String keyword,
        @NotNull YearMonth startMonth
) {
}
