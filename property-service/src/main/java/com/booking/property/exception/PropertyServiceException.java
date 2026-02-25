package com.booking.property.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PropertyServiceException extends RuntimeException {

    private final HttpStatus status;

    public PropertyServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
