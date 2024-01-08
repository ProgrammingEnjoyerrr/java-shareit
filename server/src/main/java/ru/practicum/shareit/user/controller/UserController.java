package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private static final String USER_ID_LOG_PLACEHOLDER = "userId = {}";
    private static final String REQUEST_BODY_LOG_PLACEHOLDER = "request body: {}";

    private final UserService userService;

    @PostMapping
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        log.info("got request POST /users");
        log.info(REQUEST_BODY_LOG_PLACEHOLDER, userDto);

        return userService.createUser(userDto);
    }

    @PatchMapping(value = "/{userId}")
    public UserUpdateDto updateUser(@PathVariable("userId") Long userId,
                             @RequestBody @Valid UserUpdateDto userUpdateDto) {
        log.info("got request PATCH /users/{userId}");
        log.info(USER_ID_LOG_PLACEHOLDER, userId);
        log.info(REQUEST_BODY_LOG_PLACEHOLDER, userUpdateDto);

        userUpdateDto.setId(userId);
        return userService.updateUser(userUpdateDto);
    }

    @GetMapping(value = "/{userId}")
    public UserDto getUserById(@PathVariable("userId") Long userId) {
        log.info("got request GET /users/{userId}");
        log.info(USER_ID_LOG_PLACEHOLDER, userId);

        return userService.getUserById(userId);
    }

    @DeleteMapping(value = "/{userId}")
    public void deleteUserById(@PathVariable("userId") Long userId) {
        log.info("got request DELETE /users/{userId}");
        log.info(USER_ID_LOG_PLACEHOLDER, userId);

        userService.deleteUserById(userId);
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        log.info("got request GET /users");

        return userService.getAllUsers();
    }
}
