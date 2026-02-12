package com.booking.user.dto;

import java.util.Optional;

public record UserPatchDto(
        Optional<String> firstName,
        Optional<String> lastName,
        Optional<String> email,

        Optional<Boolean> isDeleted
) {}
