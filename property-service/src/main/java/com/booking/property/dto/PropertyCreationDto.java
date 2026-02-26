package com.booking.property.dto;

import com.booking.property.entity.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Data for creating a new property")
public record PropertyCreationDto(
        @Schema(description = "The real estate name", example = "Cozy apartment in city center", maxLength = 255)
        @NotBlank(message = "Property name is required")
        @Size(max = 255)
        String name,

        @Schema(description = "Property description", example = "Spacious apartment with park view", maxLength = 300)
        @NotBlank(message = "Description is required")
        @Size(max = 300)
        String description,

        @Schema(description = "Address", example = "10 Green Street")
        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String address,

        @Schema(description = "City", example = "London")
        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @Schema(description = "Country", example = "United Kingdom")
        @NotBlank(message = "Country is required")
        @Size(max = 100)
        String country,

        @Schema(description = "The real estate type", example = "APARTMENT")
        @NotNull(message = "Property type is required")
        PropertyType propertyType,

        @Schema(description = "Price per night in USD", example = "150.00", minimum = "0.01")
        @NotNull(message = "Price per night is required")
        @Positive(message = "Price per night must be positive")
        BigDecimal pricePerNight,

        @Schema(description = "Maximum number of guests", example = "4", minimum = "1")
        @NotNull(message = "Guest quantity is required")
        @Min(1)
        Integer maxGuests,

        @Schema(description = "Owner ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "Owner ID is required")
        UUID ownerId
) {
}
