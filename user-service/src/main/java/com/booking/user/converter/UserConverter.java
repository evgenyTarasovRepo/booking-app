package com.booking.user.converter;

import com.booking.user.dto.UserDto;
import com.booking.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserConverter {
    public UserDto toUserDto(User source) {
        return new UserDto(
                source.getId(),
                source.getFirstName(),
                source.getLastName(),
                source.getEmail(),
                source.getCreatedAt()
        );
    }

    public List<UserDto> toUserDtoList(List<User> users) {
        return users.stream()
                .map(this::toUserDto)
                .toList();
    }
}
