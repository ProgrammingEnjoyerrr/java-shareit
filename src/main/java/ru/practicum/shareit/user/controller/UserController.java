package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    UserDto createUser(@RequestBody @Valid UserDto userDto) {
        return userService.createUser(userDto);
    }

    @PatchMapping(value = "/{userId}")
    UserDto updateUser(@PathVariable("userId") Long userId,
                       @RequestBody @Valid UserDto userDto) {

        UserDto userUpdateDto = UserDto.builder()
                .id(userId)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
        return userService.updateUser(userUpdateDto);
    }

    @GetMapping(value = "/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping(value = "/{userId}")
    UserDto deleteUserById(@PathVariable("userId") Long userId) {
        return userService.deleteUserById(userId);
    }

    @GetMapping
    Collection<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }
}
