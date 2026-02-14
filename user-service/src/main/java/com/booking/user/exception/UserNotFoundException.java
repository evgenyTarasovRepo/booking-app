package com.booking.user.exception;


import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserNotFoundException extends RuntimeException {

    private final static String USER_NOT_FOUND_MSG = "User '%s' not found.";
    private final static String USERS_NOT_FOUND_MSG = "Users '[%s]' not found.";

    private UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException forUser(UUID userId) {
        return new UserNotFoundException(String.format(USER_NOT_FOUND_MSG, userId));
    }

    public static UserNotFoundException forUserEmail(String userEmail) {
        return new UserNotFoundException(String.format(USER_NOT_FOUND_MSG, userEmail));
    }

    public static UserNotFoundException forUsers(Collection<UUID> userIds) {
        var usersStr = userIds.stream().map(UUID::toString).collect(Collectors.joining(", "));
        return new UserNotFoundException(String.format(USERS_NOT_FOUND_MSG, usersStr));
    }
}
