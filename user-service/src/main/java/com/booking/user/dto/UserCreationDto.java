package com.booking.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreationDto(
        @NotBlank(message = "First name is required")
        @Size(max = 255)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 255)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
