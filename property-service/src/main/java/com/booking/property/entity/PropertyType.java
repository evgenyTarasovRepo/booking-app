package com.booking.property.entity;

import com.booking.property.exception.PropertyServiceException;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.http.HttpStatus;

public enum PropertyType {
    HOTEL,
    APARTMENT,
    HOUSE,
    ROOM,
    BUNGALOW,
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
