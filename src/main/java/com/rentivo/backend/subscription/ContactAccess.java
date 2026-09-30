package com.rentivo.backend.subscription;

import com.rentivo.backend.listing.Listing;
import com.rentivo.backend.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "contact_accesses",
        uniqueConstraints = @UniqueConstraint(name = "uk_contact_access_user_listing",
                columnNames = {"user_id", "listing_id"}))
public class ContactAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id")
    private UserSubscription subscription;

    @Column(nullable = false)
    private Instant unlockedAt;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    public UserSubscription getSubscription() { return subscription; }
    public void setSubscription(UserSubscription subscription) { this.subscription = subscription; }
    public Instant getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Instant unlockedAt) { this.unlockedAt = unlockedAt; }
}
