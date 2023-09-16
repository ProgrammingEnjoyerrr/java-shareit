package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto updateUser(UserDto userDto);
    UserDto getUserById(Long userId);
    UserDto deleteUserById(Long userId);
    Collection<UserDto> getAllUsers();
}
