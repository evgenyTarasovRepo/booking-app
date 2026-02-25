package com.booking.property.exception;

import java.util.UUID;

public class OwnerNotFoundException extends RuntimeException {
    public OwnerNotFoundException(UUID id) {

        super("Owner with id " + id + " not found");
    }

    public static OwnerNotFoundException forOwner(UUID id) {
        return new OwnerNotFoundException(id);
    }
}
