package ru.practicum.shareit.user.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@Validated
public class UserController {

    @PostMapping
    UserDto createUser(@RequestBody @Valid UserDto userDto) {
        return null;
    }

    @PatchMapping(value = "/{userId}")
    UserDto updateUser(@PathVariable("userId") Long userId,
                       @RequestBody @Valid UserDto userDto) {
        return null;
    }

    @GetMapping(value = "/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId) {
        return null;
    }

    @DeleteMapping(value = "/{userId}")
    UserDto deleteUserById(@PathVariable("userId") Long userId) {
        return null;
    }

    @GetMapping
    Collection<UserDto> getAllUsers() {
        return null;
    }
}
