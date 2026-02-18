package com.booking.property.exception;

import java.util.UUID;

public class PropertyNotFoundException extends RuntimeException {
    public PropertyNotFoundException(String message) {

        super(message);
    }

    public static PropertyNotFoundException forProperty(UUID id) {
        return new PropertyNotFoundException("Property with id " + id + " not found");
    }
}
