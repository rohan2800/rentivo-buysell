package com.rentivo.backend.favorite;

import com.rentivo.backend.listing.Listing;
import com.rentivo.backend.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "favorites",
        uniqueConstraints = @UniqueConstraint(name = "uk_favorites_user_listing", columnNames = {"user_id", "listing_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
