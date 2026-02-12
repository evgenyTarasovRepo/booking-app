package com.booking.user.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Mappings {
    public static final String BASE_USER_URL = "/api/v1/public/users";
    public static final String GET_USER_BY_ID = "byId/{userId}";

    public static final String GET_USER_LIST_BY_ID = "list";

    public static final String GET_ALL_USERS = "all";

    public static final String UPDATE_OR_DELETE = "patch/{userId}";

    public static final String GET_USER_BY_EMAIL = "byEmail/{email}";
}
