package com.booking.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for partial user update")
public record UserPatchDto(

        @Schema(description = "New user name", example = "Updated user name")
        @Size(max = 255, message = "User name must be at most 255 characters")
        String firstName,

        @Schema(description = "New user last", example = "Updated last name")
        @Size(max = 255, message = "User last name must be at most 255 characters")
        String lastName,

        @Schema(description = "New user email", example = "newTest@gmail.com", maxLength = 255)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}
