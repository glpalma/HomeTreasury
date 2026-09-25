package com.glpalma.HomeTreasury.pluggy;

import java.math.BigDecimal;

/**
 * One payment applied to a {@link PluggyBill}. Only {@code amount} is used today — summed across
 * a bill's payments to tell whether that bill has already been settled (see
 * {@code TreasuryService.isFullyPaid}), so a paid-off past bill is never mistaken for the
 * currently pending one.
 */
public record PluggyBillPayment(BigDecimal amount) {
}
