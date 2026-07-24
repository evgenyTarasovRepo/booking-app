package com.booking.user.service.impl;

import com.booking.user.exception.UserAlreadyExistsException;
import com.booking.user.mapper.UserMapper;
import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;
import com.booking.user.entity.User;
import com.booking.user.exception.UserNotFoundException;
import com.booking.user.repository.UserRepository;
import com.booking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String MSG_USER_ALREADY_EXISTS = "User with login %s already exists";

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    @Transactional
    public UserDto create(UserCreationDto creationDto) {
        log.info("Creating user with email: {}", creationDto.email());

        verifyUserExists(creationDto);

        var user = userMapper.toUser(creationDto);
        var saveUser = userRepository.save(user);
        return userMapper.toUserDto(saveUser);
    }

    @Override
    @Transactional
    public UserDto update(UUID userId, UserPatchDto userForUpdate) {
        log.info("Updating user: {}", userId);
        var updatedUser = updateUserData(userId, userForUpdate);
        return userMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getById(UUID userId) {
        var userEntity = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> UserNotFoundException.forUser(userId));
        return userMapper.toUserDto(userEntity);
    }

    @Override
    public List<UserDto> getActiveUsersByIds(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        var users = userRepository.findByIdInAndDeletedFalse(userIds);

        return processUsers(userIds, users);
    }

    @Override
    public UserDto getByEmail(String email) {
        var userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.forUserEmail(email));
        return userMapper.toUserDto(userEntity);
    }

    @Override
    public Page<UserDto> getAll(Pageable pageable) {
        var usersPage = userRepository.findAll(pageable);
        return usersPage.map(userMapper::toUserDto);
    }

    @Override
    public List<UserDto> getAllByIds(Set<UUID> ids){
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        var users = userRepository.findByIdIn(ids);

        return processUsers(ids, users);
    }

    @Override
    @Transactional
    public UserDto changeDeleteStateForUser(UUID userId, boolean deleteState) {
        var user = changeDeleteState(userId, deleteState);

        return userMapper.toUserDto(user);
    }

    private User changeDeleteState(UUID userId, boolean deleteState) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.forUser(userId));

        user.setDeleted(deleteState);

        return user;
    }

    private List<UserDto> processUsers(Set<UUID> userIds, List<User> users) {
        Set<UUID> found = users.stream().map(User::getId).collect(Collectors.toSet());
        Set<UUID> notFound = new HashSet<>(userIds);
        notFound.removeAll(found);

        if (!notFound.isEmpty()) {
            throw UserNotFoundException.forUsers(notFound);
        }

        return userMapper.toUserDtoList(users);
    }

    private User updateUserData(UUID userId, UserPatchDto updatedUser) {
        var userEntity = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> UserNotFoundException.forUser(userId));
        if (updatedUser.firstName() != null) userEntity.setFirstName(updatedUser.firstName());
        if (updatedUser.lastName() != null) userEntity.setLastName(updatedUser.lastName());
        if (updatedUser.email() != null) userEntity.setEmail(updatedUser.email());

        return userEntity;
    }

    private void verifyUserExists(UserCreationDto user) {
        var isExists = userRepository.existsByEmail(user.email());

        if (isExists) {
            throw new UserAlreadyExistsException(MSG_USER_ALREADY_EXISTS, user.email());
        }
    }
}
