package com.booking.user.service;

import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserService {
    UserDto create(UserCreationDto creationDto);

    UserDto update(UUID userId, UserPatchDto userForUpdate);

    UserDto getById(UUID userId);

    List<UserDto> getByIds(Set<UUID> userIds);

    UserDto getByEmail(String login);

    Page<UserDto> getAll(Pageable pageable);

    List<UserDto> getAllByIds(Set<UUID> ids);

    UserDto changeDeleteStateForUser(UUID userId, Boolean deleteState);
}
