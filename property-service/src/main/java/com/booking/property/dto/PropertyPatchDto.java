package com.booking.property.dto;

import com.booking.property.entity.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Data for partial property update")
public record PropertyPatchDto(
        @Schema(description = "New property name", example = "Updated apartment name")
        @Size(max = 255, message = "Name must be at most 255 characters")
        String name,

        @Schema(description = "New property description", example = "Updated description")
        @Size(max = 300, message = "Description must be at most 300 characters")
        String description,

        @Schema(description = "New street address", example = "20 Blue Avenue")
        @Size(max = 255, message = "Address must be at most 255 characters")
        String address,

        @Schema(description = "New city", example = "Manchester")
        @Size(max = 100, message = "City must be at most 100 characters")
        String city,

        @Schema(description = "New country", example = "United Kingdom")
        @Size(max = 100, message = "Country must be at most 100 characters")
        String country,

        @Schema(description = "New property type", example = "HOUSE")
        PropertyType propertyType,

        @Schema(description = "New price per night in USD", example = "200.00")
        @Positive(message = "Price per night must be positive")
        BigDecimal pricePerNight,

        @Schema(description = "New maximum number of guests", example = "5")
        @Min(value = 1, message = "At least 1 guest required")
        Integer maxGuests
) {
}
