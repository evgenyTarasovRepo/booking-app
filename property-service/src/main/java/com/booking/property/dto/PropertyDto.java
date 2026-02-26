package com.booking.property.dto;

import com.booking.property.entity.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Property details")
public record PropertyDto(

        @Schema(description = "Unique identifier")
        UUID id,

        @Schema(description = "Property name")
        String name,

        @Schema(description = "Property description")
        String description,

        @Schema(description = "Address")
        String address,

        @Schema(description = "City")
        String city,

        @Schema(description = "Country")
        String country,

        @Schema(description = "Property type")
        PropertyType propertyType,

        @Schema(description = "Price per night")
        BigDecimal pricePerNight,

        @Schema(description = "Maximum number of guests")
        Integer maxGuests,

        @Schema(description = "Owner ID")
        UUID ownerId,

        @Schema(description = "Whether the property is active and available for booking")
        Boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {
}
