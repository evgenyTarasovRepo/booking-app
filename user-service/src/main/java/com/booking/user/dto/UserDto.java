package com.booking.user.dto;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "User details")
public record UserDto(
        @Schema(description = "Unique identifier")
        UUID id,

        @Schema(description = "User name")
        String firstName,

        @Schema(description = "User last name")
        String lastName,

        @Schema(description = "User email")
        String email,

        @Schema(description = "Creation timestamp")
        LocalDateTime userCreationDate,

        @Schema(description = "Removal status")
        Boolean isDeleted
) {

}
