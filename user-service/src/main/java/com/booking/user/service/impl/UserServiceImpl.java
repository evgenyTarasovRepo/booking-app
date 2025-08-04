package com.booking.user.service.impl;

import com.booking.user.converter.UserConverter;
import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;
import com.booking.user.entity.User;
import com.booking.user.exception.UserNotFoundException;
import com.booking.user.repository.UserRepository;
import com.booking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;


    @Override
    @Transactional
    public UserDto create(UserCreationDto creationDto) {
        log.info("Creating user with email: {}", creationDto.email());
        var user = toUserEntity(creationDto);
        var saveUser = userRepository.save(user);
        return userConverter.toUserDto(saveUser);
    }

    @Override
    @Transactional
    public UserDto update(UUID userId, UserPatchDto userForUpdate) {
        log.info("Updating user: {}", userId);
        var updatedUser = updateUserData(userId, userForUpdate);
        return userConverter.toUserDto(updatedUser);
    }

    @Override
    public UserDto getById(UUID userId) {
        var userEntity = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.forUser(userId));
        return userConverter.toUserDto(userEntity);
    }

    @Override
    public List<UserDto> getByIds(Set<UUID> userIds) {
        var users = userRepository.findByIdsAndDeleteFalse(userIds);

        if (users.isEmpty()) {
            throw UserNotFoundException.forUsers(userIds);
        }

        return userConverter.toUserDtoList(users);
    }

    @Override
    public UserDto getByEmail(String email) {
        var userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.forUserEmail(email));
        return userConverter.toUserDto(userEntity);
    }

    private User toUserEntity(UserCreationDto creationDto) {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        return new User(id, creationDto.firstName(),
                creationDto.lastName(), creationDto.email(), createdAt, false);
    }

    private User updateUserData(UUID userId, UserPatchDto updatedUser) {
        var userEntity = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.forUser(userId));
        updatedUser.firstName().ifPresent(userEntity::setFirstName);
        updatedUser.lastName().ifPresent(userEntity::setLastName);
        updatedUser.email().ifPresent(userEntity::setEmail);

        return userRepository.save(userEntity);
    }
}
