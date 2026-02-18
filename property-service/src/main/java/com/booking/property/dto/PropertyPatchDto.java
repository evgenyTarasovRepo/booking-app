package com.booking.property.dto;

import com.booking.property.entity.PropertyType;

import java.math.BigDecimal;
import java.util.UUID;

public record PropertyPatchDto(
        String name,

        String description,

        String address,

        String city,

        String country,

        PropertyType propertyType,

        BigDecimal pricePerNight,

        Integer maxGuests,

        UUID ownerId
) {
}
