package com.booking.user.service;

import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserService {
    UserDto create(UserCreationDto creationDto);

    UserDto update(UUID userId, UserPatchDto userForUpdate);

    UserDto getById(UUID userId);

    List<UserDto> getByIds(Set<UUID> userIds);

    UserDto getByEmail(String login);

    List<UserDto> getAll();
}
