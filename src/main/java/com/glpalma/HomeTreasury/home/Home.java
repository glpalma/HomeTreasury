package com.glpalma.HomeTreasury.home;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "homes")
public class Home {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal idealBalance;

    protected Home() {
    }

    public Home(String name, BigDecimal idealBalance) {
        this.name = name;
        this.idealBalance = idealBalance;
    }

    public Home(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getIdealBalance() {
        return idealBalance;
    }

    public void setIdealBalance(BigDecimal idealBalance) {
        this.idealBalance = idealBalance;
    }
}