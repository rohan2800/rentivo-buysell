package com.rentivo.backend.category;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean systemCategory = false;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC, id ASC")
    private List<CategoryField> fields = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isSystemCategory() { return systemCategory; }
    public void setSystemCategory(boolean systemCategory) { this.systemCategory = systemCategory; }
    public List<CategoryField> getFields() { return fields; }
}
