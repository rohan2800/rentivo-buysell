package com.rentivo.backend.listing;

import com.rentivo.backend.category.CategoryField;
import jakarta.persistence.*;

@Entity
@Table(name = "listing_field_values",
        uniqueConstraints = @UniqueConstraint(name = "uk_listing_field", columnNames = {"listing_id", "field_id"}))
public class ListingFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_id")
    private CategoryField field;

    @Column(nullable = false, length = 4000)
    private String value;

    public Long getId() { return id; }
    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    public CategoryField getField() { return field; }
    public void setField(CategoryField field) { this.field = field; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
