package com.booking.user.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public record UserCreationDto(
        String firstName,

        String lastName,

        String email
) {
}
