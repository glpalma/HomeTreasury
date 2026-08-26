package com.glpalma.HomeTreasury.user;

import com.glpalma.HomeTreasury.home.Home;
import jakarta.persistence.*;

@Entity
@Table(
        name = "home_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "home_id"})
)
public class HomeMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "home_id")
    private Home home;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected HomeMembership() {
    }

    public HomeMembership(AppUser user, Home home, Role role) {
        this.user = user;
        this.home = home;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Home getHome() {
        return home;
    }

    public Role getRole() {
        return role;
    }
}