package com.glpalma.HomeTreasury.bills;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One past debit the keyword would have matched. {@code amount} is the absolute value. */
public record BillPreviewMatch(LocalDate date, String description, BigDecimal amount) {
}
