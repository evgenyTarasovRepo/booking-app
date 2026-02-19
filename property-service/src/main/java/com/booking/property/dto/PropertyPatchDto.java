package com.booking.property.dto;

import com.booking.property.entity.PropertyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PropertyPatchDto(
        @Size(max = 255, message = "Name must be less than 255 characters")
        String name,

        @Size(max = 300, message = "Description must be less than 300 characters")
        String description,

        @Size(max = 255, message = "Address must be less than 255 characters")
        String address,

        @Size(max = 100, message = "City must be less than 100 characters")
        String city,

        @Size(max = 100, message = "Country must be less than 100 characters")
        String country,

        PropertyType propertyType,

        @Positive(message = "Price per night must be positive")
        BigDecimal pricePerNight,

        @Min(value = 1, message = "At least 1 guest required")
        Integer maxGuests
) {
}
