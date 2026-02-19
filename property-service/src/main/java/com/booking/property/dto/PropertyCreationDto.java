package com.booking.property.dto;

import com.booking.property.entity.PropertyType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record PropertyCreationDto(
        @NotBlank(message = "Property name is required")
        @Size(max = 255)
        String name,

        @NotBlank(message = "Description is required")
        @Size(max = 300)
        String description,

        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String address,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @NotBlank(message = "Country is required")
        @Size(max = 100)
        String country,

        @NotNull
        PropertyType propertyType,

        @NotNull(message = "Price per night is required")
        @Positive(message = "Price per night must be positive")
        BigDecimal pricePerNight,

        @NotNull(message = "Guest quantity is required")
        @Positive(message = "Guest quantity must be positive")
        @Min(1)
        Integer maxGuests,

        @NotNull(message = "Owner ID is required")
        UUID ownerId
) {
}
