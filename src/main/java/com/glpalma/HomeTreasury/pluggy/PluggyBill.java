package com.glpalma.HomeTreasury.pluggy;

import java.math.BigDecimal;
import java.util.List;

/**
 * One credit card invoice (fatura) from Pluggy's {@code GET /bills} endpoint. {@code dueDate} is
 * when the cardholder must pay {@code totalAmount} — this is the number Brazilian cardholders
 * mean by "a fatura", distinct from {@code Account.balance}, which for some connectors reflects
 * the card's total used limit (including future installments) rather than one invoice.
 * <p>
 * Pluggy does not return a not-yet-closed invoice at all (confirmed against their docs and live
 * data): between a bill's {@code dueDate} and the next {@code billClosingDate}, the most recent
 * bill in {@code results} is simply the last one that closed — it may already be fully settled.
 * {@code payments} is what lets callers tell the difference (see
 * {@code TreasuryService.isFullyPaid}) instead of mistaking a paid-off bill for the pending one.
 */
public record PluggyBill(
        String id,
        String dueDate,
        String billClosingDate,
        BigDecimal totalAmount,
        String totalAmountCurrencyCode,
        BigDecimal minimumPaymentAmount,
        Boolean allowsInstallments,
        List<PluggyBillPayment> payments
) {
}
