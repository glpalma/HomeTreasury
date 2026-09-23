package com.glpalma.HomeTreasury.home;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "home_invites")
public class HomeInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "home_id")
    private Home home;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used;

    protected HomeInvite() {
    }

    public HomeInvite(Home home, String code, Instant expiresAt) {
        this.home = home;
        this.code = code;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public Long getId() { return id; }
    public Home getHome() { return home; }
    public String getCode() { return code; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }
    public void markUsed() { this.used = true; }
}