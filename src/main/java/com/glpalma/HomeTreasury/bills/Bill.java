package com.glpalma.HomeTreasury.bills;

import com.glpalma.HomeTreasury.home.Home;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "bills")
public class Bill {

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

    /** 1–31. In shorter months the pay-by date is clamped to the last day of the month. */
    @Column(nullable = false)
    private int dueDay;

    /** Matched case- and accent-insensitively against a transaction's description. */
    @Column(nullable = false)
    private String keyword;

    /** First month this bill applies to (inclusive). */
    @Column(nullable = false)
    private YearMonth startMonth;

    /** Last month this bill applies to (inclusive); null = still active. Set by archiving. */
    private YearMonth endMonth;

    protected Bill() {
    }

    public Bill(Home home, String name, BigDecimal expectedAmount, int dueDay, String keyword, YearMonth startMonth) {
        this.home = home;
        this.name = name;
        this.expectedAmount = expectedAmount;
        this.dueDay = dueDay;
        this.keyword = keyword;
        this.startMonth = startMonth;
    }

    public void update(String name, BigDecimal expectedAmount, int dueDay, String keyword, YearMonth startMonth) {
        this.name = name;
        this.expectedAmount = expectedAmount;
        this.dueDay = dueDay;
        this.keyword = keyword;
        this.startMonth = startMonth;
    }

    /** Makes the bill stop applying after {@code lastMonth} (inclusive). */
    public void archive(YearMonth lastMonth) {
        this.endMonth = lastMonth;
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

    public int getDueDay() {
        return dueDay;
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
