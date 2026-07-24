package com.booking.user.service;

import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;
import com.booking.user.exception.UserAlreadyExistsException;
import com.booking.user.exception.UserNotFoundException;
import com.booking.user.mapper.UserMapperImpl;
import com.booking.user.repository.UserRepository;
import com.booking.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({UserServiceImpl.class, UserMapperImpl.class})
@Sql(scripts = {
        "/sql/user/user-test-data.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserServiceDataJpaTest {

    private static final UUID USER_ID_1 = UUID.fromString("901737a7-2dc4-477e-adaa-7d1df44623dd");
    private static final UUID USER_ID_2 = UUID.fromString("501737a7-2dc4-466e-adaa-7d1df33623bf");
    private static final UUID USER_ID_3 = UUID.fromString("96abd400-20d2-409f-939a-6418174b44d5");
    private static final UUID USER_ID_4 = UUID.fromString("24790ea0-c47f-46b2-b1fe-c8bd4ba60edb");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("users")
            .withUsername("user")
            .withPassword("password");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_shouldSuccessfullyCreateUser() {
        var userCreationDto =  userCreationDto("BrandNewUser", "NewLastname", "newEmail@email.com");

        var result = userService.create(userCreationDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.firstName()).isEqualTo(userCreationDto.firstName());
        assertThat(result.lastName()).isEqualTo(userCreationDto.lastName());
        assertThat(result.email()).isEqualTo(userCreationDto.email());
        assertThat(result.userCreationDate()).isNotNull();
        assertThat(result.isDeleted()).isFalse();
    }

    @Test
    void create_shouldThrowExceptionWhenUserAlreadyExists() {
        var userCreationDto =  userCreationDto("BrandNewUser", "NewLastname", "userEmail@gmail.com");

        assertThatThrownBy(() -> userService.create(userCreationDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void update_shouldSuccessfullyUpdateUser() {
        var patchDto = new UserPatchDto("UpdatedUser", "UpdatedLastname", "updatedEmail@email.com");

        var userFromDb = userRepository.findById(USER_ID_1).orElseThrow();
        var result = userService.update(USER_ID_1, patchDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userFromDb.getId());
        assertThat(result.firstName()).isEqualTo(patchDto.firstName());
        assertThat(result.lastName()).isEqualTo(patchDto.lastName());
        assertThat(result.email()).isEqualTo(patchDto.email());
    }

    @Test
    void update_shouldSuccessfullyPartiallyUpdateUser() {
        var patchDto = new UserPatchDto(null, null, "updatedEmail@email.com");

        var userFromDb = userRepository.findById(USER_ID_1).orElseThrow();
        var result = userService.update(USER_ID_1, patchDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userFromDb.getId());
        assertThat(result.firstName()).isEqualTo(userFromDb.getFirstName());
        assertThat(result.lastName()).isEqualTo(userFromDb.getLastName());
        assertThat(result.email()).isEqualTo(patchDto.email());
    }

    @Test
    void update_shouldSuccessfullySetDelete() {

        var result = userService.changeDeleteStateForUser(USER_ID_1, true);

        assertThat(result.isDeleted()).isTrue();
    }

    @Test
    void update_shouldSuccessfullyChangeDeleteFlag() {

        var result = userService.changeDeleteStateForUser(USER_ID_4, false);

        assertThat(result.isDeleted()).isFalse();
    }

    @Test
    void update_shouldThrowExceptionWhenUserNotFound_whileChangeDeleteState() {
        assertThatThrownBy(() -> userService.changeDeleteStateForUser(UUID.randomUUID(), false))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void update_shouldThrowExceptionWhenUserNotFound_whileUpdate() {
        var patchDto = new UserPatchDto("UpdatedUser", "UpdatedLastname", "updatedEmail@email.com");

        assertThatThrownBy(() -> userService.update(UUID.randomUUID(), patchDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void get_shouldSuccessfullyReturnUserById(){

        var result = userService.getById(USER_ID_1);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID_1);
        assertThat(result.firstName()).isEqualTo("UserName-1");
        assertThat(result.lastName()).isEqualTo("UserLastName-1");
        assertThat(result.email()).isEqualTo("userEmail@gmail.com");
        assertThat(result.userCreationDate()).isNotNull();
        assertThat(result.isDeleted()).isFalse();
    }

    @Test
    void get_shouldThrowExceptionWhenUserNotFoundById(){

        assertThatThrownBy(() -> userService.getById(UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void get_shouldSuccessfullyReturnUserByEmail() {
        var result = userService.getByEmail("userEmail@gmail.com");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID_1);
        assertThat(result.firstName()).isEqualTo("UserName-1");
        assertThat(result.lastName()).isEqualTo("UserLastName-1");
        assertThat(result.email()).isEqualTo("userEmail@gmail.com");
        assertThat(result.userCreationDate()).isNotNull();
        assertThat(result.isDeleted()).isFalse();
    }

    @Test
    void get_shouldThrowExceptionWhenUserNotFoundByEmail(){

        assertThatThrownBy(() -> userService.getByEmail("blablaEmail@gmail.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void get_shouldSuccessfullyReturnUsersByIds() {

        var usersIds = Set.of(USER_ID_1, USER_ID_2, USER_ID_3);

        var result = userService.getActiveUsersByIds(usersIds);

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(UserDto::id)
                .containsExactlyInAnyOrder(USER_ID_1, USER_ID_2, USER_ID_3);
        assertThat(result)
                .allSatisfy(user -> assertThat(user.isDeleted()).isFalse());
    }

    @Test
    void get_shouldThrowExceptionWhenUsersNotFoundByById() {
        var usersIds = Set.of(USER_ID_1, UUID.randomUUID(), USER_ID_3);

        assertThatThrownBy(() -> userService.getActiveUsersByIds(usersIds))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void get_shouldSuccessfullyReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 7);

        var result = userService.getAll(pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(7);
        assertThat(result.getContent().size()).isEqualTo(7);
        assertThat(result.getContent())
                .extracting(UserDto::isDeleted)
                .contains(false, true);

    }

    private UserCreationDto userCreationDto(String firstName, String lastName, String email) {
        return new UserCreationDto(firstName, lastName, email);
    }
}
