package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
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

        User created = userRepository.save(user);

        log.info("пользователь создан; id: {}", created.getId());
        return UserMapper.toUserDto(created);
    }

    @Override
    public UserUpdateDto updateUser(UserUpdateDto userUpdateDto) {
        long userId = userUpdateDto.getId();
        User oldUser = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        User userToUpdate = UserMapper.toUser(userUpdateDto);

        userToUpdate = mapUserWithNullFields(oldUser, userToUpdate);

        User updated = userRepository.save(userToUpdate);

        log.info("пользователь с id {} обновлен", updated.getId());
        return UserMapper.toUserUpdateDto(updated);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        log.info("найден пользователь с id {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
        log.info("пользователь с id {} удалён", userId);
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private User mapUserWithNullFields(final User oldUser, final User userToUpdate) {
        return User.builder()
                .id(oldUser.getId())
                .name(userToUpdate.getName() != null ? userToUpdate.getName() : oldUser.getName())
                .email(userToUpdate.getEmail() != null ? userToUpdate.getEmail() : oldUser.getEmail())
                .build();
    }

    private UserNotFoundException generateUserNotFoundException(long userId) {
        String message = "пользователь с id " + userId + " не существует";
        log.error(message);
        return new UserNotFoundException(message);
    }
}
