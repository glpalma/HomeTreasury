package com.glpalma.HomeTreasury.bills;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.YearMonth;

/** "This bill was paid in this month", asserted by the OWNER rather than found in the bank. */
@Entity
@Table(
        name = "bill_payment_marks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bill_id", "payment_month"})
)
public class BillPaymentMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    // Not plain "month": MONTH is a keyword in some SQL dialects (the test suite runs on H2).
    @Column(name = "payment_month", nullable = false)
    private YearMonth month;

    @Column(nullable = false)
    private Instant markedAt;

    protected BillPaymentMark() {
    }

    public BillPaymentMark(Bill bill, YearMonth month, Instant markedAt) {
        this.bill = bill;
        this.month = month;
        this.markedAt = markedAt;
    }

    public Long getId() {
        return id;
    }

    public Bill getBill() {
        return bill;
    }

    public YearMonth getMonth() {
        return month;
    }

    public Instant getMarkedAt() {
        return markedAt;
    }
}
