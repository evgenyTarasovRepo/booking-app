package com.booking.user.controller;

import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;
import com.booking.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Tag(name = "Users", description = "API for User Service")
public class UserController {

    private UserService userService;

    @Operation(summary = "Create user", description = "Method for user creation")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserCreationDto userCreationDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(userCreationDto));
    }

    @Operation(summary = "Get a user bu ID", description = "Returns user by unique ID")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId")UUID userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @Operation(summary = "Get all users by IDs", description = "Returns list of users by IDs")
    @PostMapping("/batch")
    public ResponseEntity<List<UserDto>> getUsersByIds(@RequestBody Set<UUID> usersIds) {
        return ResponseEntity.ok(userService.getByIds(usersIds));
    }

    @Operation(summary = "Get all users", description = "Returns a paginated list of all users")
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getAll(PageRequest.of(page, size)));
    }

    @Operation(summary = "Update user by ID", description = "Partially update the user. Send only the fields that need to be changed")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("userId") UUID userId,
                                                         @RequestBody UserPatchDto userPatchDto) {
        return ResponseEntity.ok(userService.update(userId, userPatchDto));
    }

    @Operation(summary = "Get user by email", description = "Returns the user by email")
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email) {
        return  ResponseEntity.ok(userService.getByEmail(email));
    }

    @Operation(summary = "Logical delete user", description = "Change logical delete status for user")
    @PatchMapping("/{userId}/delete")
    public ResponseEntity<UserDto> changeDeleteStateForUser(@PathVariable("userId") UUID userId, @RequestParam("deleted") Boolean deleted) {
        return ResponseEntity.ok(userService.changeDeleteStateForUser(userId, deleted));
    }
}
