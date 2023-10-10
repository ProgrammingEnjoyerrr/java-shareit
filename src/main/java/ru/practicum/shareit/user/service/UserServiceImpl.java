package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.exception.NonUniqueEmailException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        ensureEmailUnique(user.getEmail(), userDto.getId());

        User created = userRepository.createUser(user);

        log.info("пользователь создан; id: {}", created.getId());
        return UserMapper.toUserDto(created);
    }

    @Override
    public UserUpdateDto updateUser(UserUpdateDto userUpdateDto) {
        User userToUpdate = UserMapper.toUser(userUpdateDto);

        ensureUserExists(userToUpdate.getId());
        ensureEmailUnique(userUpdateDto.getEmail(), userUpdateDto.getId());

        User updated = userRepository.updateUser(userToUpdate);

        log.info("пользователь с id {} обновлен", updated.getId());
        return UserMapper.toUserUpdateDto(updated);
    }

    @Override
    public UserDto getUserById(Long userId) {
        ensureUserExists(userId);

        User user = userRepository.getUserById(userId);

        log.info("найден пользователь с id {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto deleteUserById(Long userId) {
        ensureUserExists(userId);

        User deleted = userRepository.deleteUserById(userId);

        log.info("пользователь с id {} удалён", deleted.getId());
        return UserMapper.toUserDto(deleted);
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.isUserExists(userId)) {
            String message = "пользователь с id " + userId + " не существует";
            log.error(message);
            throw new UserNotFoundException(message);
        }

        log.info("пользователь с id {} найден", userId);
    }

    private void ensureEmailUnique(String email, Long userId) {
        if (!userRepository.isEmailUnique(email, userId)) {
            String message = "Email " + email + " уже занят";
            log.error(message);
            throw new NonUniqueEmailException(message);
        }
    }
}
