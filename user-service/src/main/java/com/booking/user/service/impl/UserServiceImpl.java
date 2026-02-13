package com.booking.user.service.impl;

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

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    @Transactional
    public UserDto create(UserCreationDto creationDto) {
        log.info("Creating user with email: {}", creationDto.email());
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
        var userEntity = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> UserNotFoundException.forUser(userId));
        return userMapper.toUserDto(userEntity);
    }

    @Override
    public List<UserDto> getByIds(Set<UUID> userIds) {
        var users = userRepository.findByIdInAndIsDeletedFalse(userIds);

        if (users.isEmpty()) {
            throw UserNotFoundException.forUsers(userIds);
        }

        return userMapper.toUserDtoList(users);
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
        var users = userRepository.findByIdIn(ids);

        if (users.isEmpty()) {
            throw UserNotFoundException.forUsers(ids);
        }

        return userMapper.toUserDtoList(users);
    }

    @Override
    @Transactional
    public UserDto changeDeleteStateForUser(UUID userId, Boolean deleteState) {
        var user = changeDeleteState(userId, deleteState);

        return userMapper.toUserDto(user);
    }

    private User changeDeleteState(UUID userId, Boolean deleteState) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.forUser(userId));

        user.setIsDeleted(deleteState);

        return userRepository.saveAndFlush(user);
    }


    private User updateUserData(UUID userId, UserPatchDto updatedUser) {
        var userEntity = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> UserNotFoundException.forUser(userId));
        if (updatedUser.firstName() != null) userEntity.setFirstName(updatedUser.firstName());
        if (updatedUser.lastName() != null) userEntity.setLastName(updatedUser.lastName());
        if (updatedUser.email() != null) userEntity.setEmail(updatedUser.email());

        return userRepository.save(userEntity);
    }
}
