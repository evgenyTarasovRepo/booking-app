package com.booking.property.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "properties")
@AllArgsConstructor
@NoArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "country", nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;

    @Column(name = "pricePerNight", nullable = false)
    private BigDecimal pricePerNight;

    @Column(name = "maxGuests", nullable = false)
    private Integer maxGuests;

    @Column(name = "ownerId", nullable = false)
    private UUID ownerId;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    public Property(String name, String description, String address, String city, String country, PropertyType propertyType, BigDecimal pricePerNight, Integer maxGuests, UUID ownerId, Boolean isActive) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.city = city;
        this.country = country;
        this.propertyType = propertyType;
        this.pricePerNight = pricePerNight;
        this.maxGuests = maxGuests;
        this.ownerId = ownerId;
        this.isActive = isActive;
    }


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
