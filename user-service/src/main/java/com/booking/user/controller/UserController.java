package com.booking.user.controller;

import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;
import com.booking.user.service.UserService;
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
public class UserController {

    private UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserCreationDto userCreationDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(userCreationDto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId")UUID userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<UserDto>> getUsersByIds(@RequestBody Set<UUID> usersIds) {
        return ResponseEntity.ok(userService.getByIds(usersIds));
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getAll(PageRequest.of(page, size)));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("userId") UUID userId,
                                                         @RequestBody UserPatchDto userPatchDto) {
        return ResponseEntity.ok(userService.update(userId, userPatchDto));
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email) {
        return  ResponseEntity.ok(userService.getByEmail(email));
    }

    @PatchMapping("/{userId}/delete")
    public ResponseEntity<UserDto> changeDeleteStateForUser(@PathVariable("userId") UUID userId, @RequestParam("deleted") Boolean deleted) {
        return ResponseEntity.ok(userService.changeDeleteStateForUser(userId, deleted));
    }
}
