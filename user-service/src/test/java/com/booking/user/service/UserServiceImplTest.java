package com.booking.user.service;

import com.booking.user.mapper.UserMapper;
import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;
import com.booking.user.entity.User;
import com.booking.user.exception.UserNotFoundException;
import com.booking.user.repository.UserRepository;
import com.booking.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    private final UUID userId = UUID.randomUUID();
    private final String userName = "userName";
    private final String userLastname = "userLastname";
    private final String userEmail = "userEmail@yahoo.com";
    private final LocalDateTime creationDate = LocalDateTime.now();

    private final UserDto userDto = new UserDto(userId, userName, userLastname, userEmail, creationDate, false);

    private final UserCreationDto userCreationDto = new UserCreationDto(userName, userLastname, userEmail);

    private User getUser() {
        return new User(userId, userName, userLastname, userEmail, creationDate, false);
    }

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper);
    }

    @Test
    void shouldCreateUser() {
        var user = createUser(userEmail, false);

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUser(userCreationDto)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        var response = userService.create(userCreationDto);

        assertThat(response).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    void shouldUpdateUser() {
        var user = createUser(userEmail, false);
        UserPatchDto updateUser = new UserPatchDto("updatedUserName", "updatedLastName",
                null);
        UserDto updatedDto = new UserDto(user.getId(), "updatedUserName", "updatedLastName", user.getEmail(),
                user.getCreatedAt(), user.getIsDeleted());

        when(userRepository.findByIdAndIsDeletedFalse(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(updatedDto);


        var result = userService.update(user.getId(), updateUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("updatedUserName", saved.getFirstName());
        assertEquals("updatedLastName", saved.getLastName());
        assertEquals(userEmail, saved.getEmail());
        assertFalse(saved.getIsDeleted());

        assertEquals("updatedUserName", result.firstName());
        assertEquals("updatedLastName", result.lastName());
        assertEquals(userEmail, result.email());
        assertFalse(result.isDeleted());
    }

    @Test
    void shouldReturnUserById() {
        var user = createUser(userEmail, false);

        when(userRepository.findByIdAndIsDeletedFalse(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        var result = userService.getById(user.getId());

        assertThat(result).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    void shouldReturnUserByEmail() {
        var user = createUser(userEmail, false);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        var result = userService.getByEmail(user.getEmail());

        assertThat(result.email()).isEqualTo(userEmail);
        assertThat(result).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    void shouldReturnUsersByIds() {
        List<User> users = List.of(createUser(userEmail, false), createUser(userEmail, false), createUser(userEmail, false));

        Set<UUID> ids = Set.of(users.get(0).getId(), users.get(1).getId(), users.get(2).getId());

        List<UserDto> userDtoList = List.of(toDto(users.get(0)), toDto(users.get(1)), toDto(users.get(2)));

        when(userRepository.findByIdInAndIsDeletedFalse(ids)).thenReturn(users);
        when(userMapper.toUserDtoList(users)).thenReturn(userDtoList);

        var result = userService.getByIds(ids);

        assertThat(result)
                .hasSize(userDtoList.size())
                .usingRecursiveComparison()
                .isEqualTo(userDtoList);
    }

    @Test
    void shouldReturnAllUsersByIds() {
        List<User> users = List.of(createUser(userEmail, false), createUser(userEmail, false), createUser(userEmail, true));

        Set<UUID> ids = Set.of(users.get(0).getId(), users.get(1).getId(), users.get(2).getId());

        List<UserDto> userDtoList = List.of(toDto(users.get(0)), toDto(users.get(1)), toDto(users.get(2)));

        when(userRepository.findByIdIn(ids)).thenReturn(users);
        when(userMapper.toUserDtoList(users)).thenReturn(userDtoList);

        var result = userService.getAllByIds(ids);

        assertThat(result)
                .hasSize(userDtoList.size())
                .usingRecursiveComparison()
                .isEqualTo(userDtoList);
    }

    @Test
    void shouldReturnAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(
                createUser(userEmail, false),
                createUser(userEmail, true),
                createUser(userEmail, true));
        List<UserDto> userDtoList = List.of(
                toDto(users.get(0)),
                toDto(users.get(1)),
                toDto(users.get(2)));
        Page<User> userPage = new PageImpl<>(users, pageable, userDtoList.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        when(userMapper.toUserDto(users.get(0))).thenReturn(userDtoList.get(0));
        when(userMapper.toUserDto(users.get(1))).thenReturn(userDtoList.get(1));
        when(userMapper.toUserDto(users.get(2))).thenReturn(userDtoList.get(2));

        var result = userService.getAll(pageable);

        // Дополнительные проверки пагинации
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(0);  // номер страницы
        assertThat(result.getSize()).isEqualTo(10);   // размер страницы
    }

    @Test
    void shouldSetDeleteTrueStateForUser() {
        var user = createUser(userEmail, false);
        var deleteDto = new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getCreatedAt(), true);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(deleteDto);

        var result = userService.changeDeleteStateForUser(user.getId(), true);

        assertThat(result).usingRecursiveComparison().isEqualTo(deleteDto);
        assertThat(user.getIsDeleted()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUserByIdNotFound() {
        var user = createUser(userEmail, false);

        var errMsg = String.format("User '%s' not found.", user.getId());

        assertThatThrownBy(() -> userService.getById(user.getId())).isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> userService.getById(user.getId())).hasMessage(errMsg);
    }

    @Test
    void shouldThrowExceptionWhenUserByEmailNotFound() {
        var user = createUser(userEmail, false);

        var errMsg = String.format("User '%s' not found.", user.getEmail());

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail(user.getEmail())).isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> userService.getByEmail(user.getEmail())).hasMessage(errMsg);
    }

    @Test
    void shouldThrowExceptionWhenUsersByIds() {
        Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(userRepository.findByIdInAndIsDeletedFalse(ids)).thenReturn(List.of());

        assertThatThrownBy(() -> userService.getByIds(ids))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of();

        Page<User> userPage = new PageImpl<>(users, pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        var result = userService.getAll(pageable);

        assertThat(result).isEmpty();
    }

    private User createUser(String email, boolean isDeleted) {
        return new User(UUID.randomUUID(), "name", "lastName", email, LocalDateTime.now(), isDeleted);
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getCreatedAt(), user.getIsDeleted());
    }

}
