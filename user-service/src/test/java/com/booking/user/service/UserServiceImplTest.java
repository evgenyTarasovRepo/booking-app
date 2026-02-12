package com.booking.user.service;

import com.booking.user.converter.UserConverter;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConverter userConverter;

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
        userService = new UserServiceImpl(userRepository, userConverter);
    }

    @Test
    void shouldCreateUser() {
        var user = createUser(userEmail, false);

        when(userRepository.save(user)).thenReturn(user);
        when(userConverter.toUserDto(user)).thenReturn(userDto);

        var response = userService.create(userCreationDto);

        assertThat(response).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    void shouldUpdateUser() {
        var user = createUser(userEmail, false);
        UserPatchDto updateUser = new UserPatchDto(Optional.of("updatedUserName"), Optional.of("updatedLastName"),
                Optional.empty(), Optional.empty());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));


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

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userConverter.toUserDto(user)).thenReturn(userDto);

        var result = userService.getById(user.getId());

        assertThat(result).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    void shouldReturnUserByEmail() {
        var user = createUser(userEmail, false);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userConverter.toUserDto(user)).thenReturn(userDto);

        var result = userService.getById(user.getId());

        assertThat(result.email()).isEqualTo(userEmail);
        assertThat(result).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    void shouldReturnUsersByIds() {
        List<User> users = List.of(createUser(userEmail, false), createUser(userEmail, false), createUser(userEmail, false));

        Set<UUID> ids = Set.of(users.get(0).getId(), users.get(1).getId(), users.get(2).getId());

        List<UserDto> userDtoList = List.of(toDto(users.get(0)), toDto(users.get(1)), toDto(users.get(2)));

        when(userRepository.findByIdsAndDeleteFalse(ids)).thenReturn(users);
        when(userConverter.toUserDtoList(users)).thenReturn(userDtoList);

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
        when(userConverter.toUserDtoList(users)).thenReturn(userDtoList);

        var result = userService.getAllByIds(ids);

        assertThat(result)
                .hasSize(userDtoList.size())
                .usingRecursiveComparison()
                .isEqualTo(userDtoList);
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = List.of(createUser(userEmail, false), createUser(userEmail, true), createUser(userEmail, true));

        Set<UUID> ids = Set.of(users.get(0).getId(), users.get(1).getId(), users.get(2).getId());

        List<UserDto> userDtoList = List.of(toDto(users.get(0)), toDto(users.get(1)), toDto(users.get(2)));

        when(userRepository.findAll()).thenReturn(users);
        when(userConverter.toUserDtoList(users)).thenReturn(userDtoList);

        var result = userService.getAllByIds(ids);

        assertThat(result)
                .hasSize(userDtoList.size())
                .usingRecursiveComparison()
                .isEqualTo(userDtoList);
    }

    @Test
    void shouldThrowExceptionWhenUserByIdNotFound() {
        var user = createUser(userEmail, false);

        var errMsg = String.format("User '%s' not found.", user.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

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
        List<User> users = List.of(createUser(userEmail, false), createUser(userEmail, true), createUser(userEmail, true));

        Set<UUID> ids = Set.of(users.get(0).getId(), users.get(1).getId(), users.get(2).getId());

        var errMsg = String.format("Users '%s' not found.", ids);

        when(userRepository.findByIdsAndDeleteFalse(ids)).thenReturn(List.of());

        assertThatThrownBy(() -> userService.getByIds(ids))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(errMsg);
    }

    @Test
    void shouldThrowExceptionWhenGetAllUsers() {
        List<User> users = List.of(createUser(userEmail, false), createUser(userEmail, true), createUser(userEmail, true));

        Set<UUID> ids = Set.of(users.get(0).getId(), users.get(1).getId(), users.get(2).getId());
        var errMsg = String.format("Users '%s' not found.", ids);

        when(userRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> userService.getAll())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(errMsg);
    }

    private User createUser(String email, boolean isDeleted) {
        return new User(UUID.randomUUID(), "name", "lastName", email, LocalDateTime.now(), isDeleted);
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getCreatedAt(), user.getIsDeleted());
    }

}
