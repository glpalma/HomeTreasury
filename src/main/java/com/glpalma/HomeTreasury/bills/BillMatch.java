package com.glpalma.HomeTreasury.bills;

import com.glpalma.HomeTreasury.treasury.BankTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Every transaction that matched a bill's (or income's) keyword this month, combined into one payment.
 *
 * @param payments      One entry per matching transaction, oldest first.
 * @param amount        Sum of every payment's amount.
 * @param amountDiffers True when {@code amount} is more than 15% away from the expected amount.
 */
public record BillMatch(
        List<Payment> payments,
        BigDecimal amount,
        boolean amountDiffers
) {
    private static final BigDecimal TOLERANCE = new BigDecimal("0.15");

    /** One transaction that counted toward the payment. */
    public record Payment(String transactionId, LocalDate date, String description, BigDecimal amount) {
        static Payment from(BankTransaction tx) {
            BigDecimal actual = tx.amount() == null ? BigDecimal.ZERO : tx.amount().abs();
            return new Payment(tx.id(), tx.date(), tx.description(), actual);
        }
    }

    public static BillMatch of(List<BankTransaction> transactions, BigDecimal expectedAmount) {
        List<Payment> payments = transactions.stream().map(Payment::from).toList();
        BigDecimal total = payments.stream().map(Payment::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allowedGap = expectedAmount.multiply(TOLERANCE).setScale(2, RoundingMode.HALF_UP);
        boolean differs = total.subtract(expectedAmount).abs().compareTo(allowedGap) > 0;
        return new BillMatch(payments, total, differs);
    }
}
