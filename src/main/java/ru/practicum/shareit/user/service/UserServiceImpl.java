package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        User created = userRepository.createUser(user);
        return UserMapper.toUserDto(created);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User userToUpdate = UserMapper.toUser(userDto);
        Optional<User> updatedOpt = userRepository.updateUser(userToUpdate);
        return UserMapper.toUserDto(updatedOpt.get());
    }

    @Override
    public UserDto getUserById(Long userId) {
        Optional<User> userOpt = userRepository.getUserById(userId);
        return UserMapper.toUserDto(userOpt.get());
    }

    @Override
    public UserDto deleteUserById(Long userId) {
        Optional<User> deletedOpt = userRepository.deleteUserById(userId);
        return UserMapper.toUserDto(deletedOpt.get());
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
