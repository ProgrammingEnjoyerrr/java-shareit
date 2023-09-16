package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserUpdateDto updateUser(UserUpdateDto userUpdateDto);
    UserDto getUserById(Long userId);
    UserDto deleteUserById(Long userId);
    Collection<UserDto> getAllUsers();
}
