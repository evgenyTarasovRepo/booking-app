package com.booking.property.dto;


import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,

        String firstName,

        String lastName,

        String email,

        LocalDateTime userCreationDate,

        Boolean isDeleted
) {

}
