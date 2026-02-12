package com.booking.user.controller;

import com.booking.user.constant.Mappings;
import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.dto.UserPatchDto;
import com.booking.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(Mappings.BASE_USER_URL)
@AllArgsConstructor
public class UserController {

    private UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreationDto userCreationDto) {
        return ResponseEntity.ok(userService.create(userCreationDto));
    }

    @GetMapping(Mappings.GET_USER_BY_ID)
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId")UUID userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @PostMapping(Mappings.GET_USER_LIST_BY_ID)
    public ResponseEntity<List<UserDto>> getUserByIds(@RequestBody Set<UUID> usersIds) {
        return ResponseEntity.ok(userService.getByIds(usersIds));
    }

    @GetMapping(Mappings.GET_ALL_USERS)
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PatchMapping(Mappings.UPDATE_OR_DELETE)
    public ResponseEntity<UserDto> updateOrLogicalDelete(@PathVariable("userId") UUID userId,
                                                         @RequestBody UserPatchDto userPatchDto) {
        return ResponseEntity.ok(userService.update(userId, userPatchDto));
    }

    @GetMapping(Mappings.GET_USER_BY_EMAIL)
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return  ResponseEntity.ok(userService.getByEmail(email));
    }
}
