package com.booking.user.controller;

import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;
import com.booking.user.exception.UserNotFoundException;
import com.booking.user.exception.exceptionhandler.GlobalExceptionHandler;
import com.booking.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();
    private final UserDto userDto = new UserDto(userId, "TestName", "TestLastName",
            "test@email.com", LocalDateTime.now(), false);


    @Test
    void createUser_Success() throws Exception {
        UserCreationDto creationDto = new UserCreationDto(userDto.firstName(), userDto.lastName(), userDto.email());

        when(userService.create(any(UserCreationDto.class))).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("TestName"));
    }

    @Test
    void createUser_ValidationError_BlankFirstName() throws Exception {
        UserCreationDto invalidDto = new UserCreationDto("", userDto.lastName(), userDto.email());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());

        verify(userService, never()).create(any());
    }

    @Test
    void createUser_ValidationError_InvalidEmail() throws Exception {
        UserCreationDto invalidDto = new UserCreationDto(userDto.firstName(), userDto.lastName(), "incorrect-email");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    @Test
    void createUser_DuplicateEmail() throws Exception {
        UserCreationDto invalidDto = new UserCreationDto(userDto.firstName(), userDto.lastName(), "existing@test.com");

        when(userService.create(any())).thenThrow(new DataIntegrityViolationException("Duplicate email"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_Success() throws Exception {
        UserPatchDto patchDto = new UserPatchDto("updatedName", "updatedLastName", "updatedEmail@email.com");
        UserDto userForUpdate = new UserDto(userId, patchDto.firstName(), patchDto.lastName(),
                patchDto.email(), LocalDateTime.now(), false);

        when(userService.update(eq(userId), any(UserPatchDto.class))).thenReturn(userForUpdate);

        mockMvc.perform(patch("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("updatedName"));
    }

    @Test
    void updateUser_NotFound() throws Exception {
        UserPatchDto patchDto = new UserPatchDto("updatedName", "updatedLastName", "updatedEmail@email.com");

        when(userService.update(eq(userId), any(UserPatchDto.class))).thenThrow(UserNotFoundException.forUser(userId));

        mockMvc.perform(patch("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUser_Success() throws Exception {
        when(userService.getById(userId)).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.id().toString()))
                .andExpect(jsonPath("$.firstName").value(userDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.lastName()))
                .andExpect(jsonPath("$.email").value(userDto.email()));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        Page<UserDto> page = new PageImpl<>(List.of(userDto));
        when(userService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(userDto.id().toString()))
                .andExpect(jsonPath("$.content[0].firstName").value(userDto.firstName()))
                .andExpect(jsonPath("$.content[0].lastName").value(userDto.lastName()))
                .andExpect(jsonPath("$.content[0].email").value(userDto.email()));
    }

    @Test
    void getUsersByIds_Success() throws Exception {
        Set<UUID> userIds = Set.of(userId);
        when(userService.getByIds(userIds)).thenReturn(List.of(userDto));

        mockMvc.perform(post("/api/v1/users/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(userDto.id().toString()))
                .andExpect(jsonPath("$[0].firstName").value(userDto.firstName()))
                .andExpect(jsonPath("$[0].lastName").value(userDto.lastName()))
                .andExpect(jsonPath("$[0].email").value(userDto.email()));
    }

    @Test
    void getUserByEmail_Success() throws Exception {
        when(userService.getByEmail(userDto.email())).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users//by-email/{email}", userDto.email()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.id().toString()))
                .andExpect(jsonPath("$.firstName").value(userDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.lastName()))
                .andExpect(jsonPath("$.email").value(userDto.email()));
    }

    @Test
    void getUser_NotFound() throws Exception {
        when(userService.getById(userId)).thenThrow(UserNotFoundException.forUser(userId));

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void getUserByEmail_NotFound() throws Exception {
        when(userService.getByEmail(userDto.email())).thenThrow(UserNotFoundException.forUserEmail(userDto.email()));

        mockMvc.perform(get("/api/v1/users//by-email/{email}", userDto.email()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void deleteUser_Success() throws Exception {
        UserDto dto = new UserDto(UUID.randomUUID(), "name", "lastName", "email@test.com", LocalDateTime.now(), true);
        when(userService.changeDeleteStateForUser(dto.id(), true)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/users/{userId}/delete", dto.id())
                        .param("deleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDeleted").value(true));

        verify(userService).changeDeleteStateForUser(dto.id(), true);
    }

    @Test
    void deleteUser_NotFound() throws Exception {
        UserDto dto = new UserDto(UUID.randomUUID(), "name", "lastName", "email@test.com", LocalDateTime.now(), true);
        when(userService.changeDeleteStateForUser(dto.id(), true)).thenThrow(UserNotFoundException.forUser(dto.id()));

        mockMvc.perform(patch("/api/v1/users/{userId}/delete", dto.id())
                        .param("deleted", "true"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());

    }
}
