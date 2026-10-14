package com.glpalma.HomeTreasury.incomes;

import com.glpalma.HomeTreasury.home.Home;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "expected_incomes")
public class ExpectedIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "home_id")
    private Home home;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal expectedAmount;

    /** 1–31: the day the money is expected by. Clamped to the month's last day in shorter months. */
    @Column(nullable = false)
    private int expectedDay;

    /** Matched case- and accent-insensitively against a CREDIT's description / payer name. */
    @Column(nullable = false)
    private String keyword;

    /** First month this income applies to (inclusive). */
    @Column(nullable = false)
    private YearMonth startMonth;

    /** Last month it applies to (inclusive); null = still active. */
    private YearMonth endMonth;

    protected ExpectedIncome() {
    }

    public ExpectedIncome(
            Home home,
            String name,
            BigDecimal expectedAmount,
            int expectedDay,
            String keyword,
            YearMonth startMonth
    ) {
        this.home = home;
        this.name = name;
        this.expectedAmount = expectedAmount;
        this.expectedDay = expectedDay;
        this.keyword = keyword;
        this.startMonth = startMonth;
    }

    public Long getId() {
        return id;
    }

    public Home getHome() {
        return home;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public int getExpectedDay() {
        return expectedDay;
    }

    public String getKeyword() {
        return keyword;
    }

    public YearMonth getStartMonth() {
        return startMonth;
    }

    public YearMonth getEndMonth() {
        return endMonth;
    }
}
