package com.glpalma.HomeTreasury.treasury;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One movement on a BANK account, in the shape the rest of HomeTreasury uses.
 *
 * @param type            Pluggy's {@code DEBIT} (money out) or {@code CREDIT} (money in).
 * @param status          {@code POSTED} or {@code PENDING}.
 * @param counterpartName For a CREDIT the payer's name, for a DEBIT the receiver's name; null
 *                        when Pluggy does not provide payment data for this transaction.
 */
public record BankTransaction(
        String id,
        String accountId,
        String accountName,
        LocalDate date,
        String description,
        String counterpartName,
        BigDecimal amount,
        String type,
        String status,
        String operationType
) {
}
