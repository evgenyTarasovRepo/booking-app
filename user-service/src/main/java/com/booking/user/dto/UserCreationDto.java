package com.booking.user.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for creating new user")
public record UserCreationDto(
        @Schema(description = "User name", example = "John", maxLength = 255)
        @NotBlank(message = "First name is required")
        @Size(max = 255)
        String firstName,

        @Schema(description = "User last name", example = "Doe", maxLength = 255)
        @NotBlank(message = "Last name is required")
        @Size(max = 255)
        String lastName,

        @Schema(description = "User email", example = "test@gmail.com", maxLength = 255)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
