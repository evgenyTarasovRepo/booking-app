package com.booking.property.exception;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class PropertyNotFoundException extends RuntimeException {
    private PropertyNotFoundException(String message) {

        super(message);
    }

    public static PropertyNotFoundException forProperty(UUID id) {
        return new PropertyNotFoundException("Property with id " + id + " not found");
    }

    public static PropertyNotFoundException forProperties(Collection<UUID> ids) {
        var properties = ids.stream().map(UUID::toString).collect(Collectors.joining(", "));
        return new PropertyNotFoundException("Property with ids: " + properties + " not found");
    }
}
