package com.booking.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message, String login) {
        super(String.format(message, login));
    }
}
