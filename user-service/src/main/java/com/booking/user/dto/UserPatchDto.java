package com.booking.user.dto;

public record UserPatchDto(
        String firstName,
        String lastName,
        String email
) {}
