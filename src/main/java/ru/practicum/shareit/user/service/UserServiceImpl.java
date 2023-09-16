package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        userRepository.createUser(user);
        return null;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        //
        return null;
    }

    @Override
    public UserDto getUserById(Long userId) {
        return null;
    }

    @Override
    public UserDto deleteUserById(Long userId) {
        return null;
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return null;
    }
}
