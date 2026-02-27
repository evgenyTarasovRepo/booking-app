package com.booking.property.entity;

import com.booking.property.exception.PropertyServiceException;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@Schema(description = "Type of property")
public enum PropertyType {
    @Schema(description = "Hotel room or suite")
    HOTEL,

    @Schema(description = "Apartment or flat")
    APARTMENT,

    @Schema(description = "Entire house")
    HOUSE,

    @Schema(description = "Single room in shared property")
    ROOM,

    @Schema(description = "Small vacation house, typically on beach")
    BUNGALOW,

    @Schema(description = "Luxury standalone house with amenities")
    VILLA;

    @JsonCreator
    public static PropertyType fromString(String propertyType) throws PropertyServiceException {
        try {
            return PropertyType.valueOf(propertyType);
        } catch (IllegalArgumentException e) {
            var msg = "Failed to create Property from string: %s".formatted(propertyType);
            throw new PropertyServiceException(msg, HttpStatus.BAD_REQUEST);
        }
    }
}
