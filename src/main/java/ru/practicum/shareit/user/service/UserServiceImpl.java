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

        User created = userRepository.save(user);

        log.info("пользователь создан; id: {}", created.getId());
        return UserMapper.toUserDto(created);
    }

    @Override
    public UserUpdateDto updateUser(UserUpdateDto userUpdateDto) {
        long userId = userUpdateDto.getId();
        User userToUpdate = UserMapper.toUser(userUpdateDto);
        User oldUser = userRepository.findById(userToUpdate.getId())
                .orElseThrow(() -> new UserNotFoundException("пользователь с id " + userId + " не существует"));
        userToUpdate = mapUserWithNullFields(oldUser, userToUpdate);

//        ensureUserExists(userToUpdate.getId());
//        ensureEmailUnique(userUpdateDto.getEmail(), userUpdateDto.getId());

        User updated = userRepository.save(userToUpdate);

        log.info("пользователь с id {} обновлен", updated.getId());
        return UserMapper.toUserUpdateDto(updated);
    }

    @Override
    public UserDto getUserById(Long userId) {
        ensureUserExists(userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(""));

        log.info("найден пользователь с id {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        ensureUserExists(userId);

        userRepository.deleteById(userId);

        log.info("пользователь с id {} удалён", userId);
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            String message = "пользователь с id " + userId + " не существует";
            log.error(message);
            throw new UserNotFoundException(message);
        }

        log.info("пользователь с id {} найден", userId);
    }

    private void ensureEmailUnique(String email, Long userId) {
//        if (!userRepository.isEmailUnique(email, userId)) {
//            String message = "Email " + email + " уже занят";
//            log.error(message);
//            throw new NonUniqueEmailException(message);
//        }
    }

    private User mapUserWithNullFields(User oldUser, User userToUpdate) {
        return User.builder()
                .id(oldUser.getId())
                .name(userToUpdate.getName() != null ? userToUpdate.getName() : oldUser.getName())
                .email(userToUpdate.getEmail() != null ? userToUpdate.getEmail() : oldUser.getEmail())
                .build();
    }
}
