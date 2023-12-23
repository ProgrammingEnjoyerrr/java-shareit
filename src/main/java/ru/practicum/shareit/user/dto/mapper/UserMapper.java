package ru.practicum.shareit.user.dto.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class UserMapper {
    public User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User toUser(UserUpdateDto userUpdateDto) {
        return User.builder()
                .id(userUpdateDto.getId())
                .name(userUpdateDto.getName())
                .email(userUpdateDto.getEmail())
                .build();
    }

    public UserUpdateDto toUserUpdateDto(User user) {
        return UserUpdateDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
