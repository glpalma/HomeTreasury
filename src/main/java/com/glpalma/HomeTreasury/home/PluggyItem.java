package com.glpalma.HomeTreasury.home;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "pluggy_items")
public class PluggyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "home_id", unique = true)
    private Home home;

    @Column(nullable = false, unique = true)
    private String itemId;

    private String connectorName;

    @Column(nullable = false)
    private Instant linkedAt;

    protected PluggyItem() {
    }

    public PluggyItem(Home home, String itemId, String connectorName, Instant linkedAt) {
        this.home = home;
        this.itemId = itemId;
        this.connectorName = connectorName;
        this.linkedAt = linkedAt;
    }

    public Long getId() {
        return id;
    }

    public Home getHome() {
        return home;
    }

    public String getItemId() {
        return itemId;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }
}
