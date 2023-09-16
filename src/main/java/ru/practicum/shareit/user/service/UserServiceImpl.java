package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.exception.NonUniqueEmailException;
import ru.practicum.shareit.user.exception.UserDoesntExistException;
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

        boolean isUnique = userRepository.isEmailUnique(user.getEmail(), userDto.getId());
        if (!isUnique) {
            throw new NonUniqueEmailException("уже было");
        }

        User created = userRepository.createUser(user);
        return UserMapper.toUserDto(created);
    }

    @Override
    public UserUpdateDto updateUser(UserUpdateDto userUpdateDto) {
        User userToUpdate = UserMapper.toUser(userUpdateDto);

        if (!userRepository.isUserExists(userToUpdate.getId())) {
            throw new UserDoesntExistException("пользователь с таким id не существует");
        }

        boolean isUnique = userRepository.isEmailUnique(userUpdateDto.getEmail(), userUpdateDto.getId());
        if (!isUnique) {
            throw new NonUniqueEmailException("уже было");
        }

        Optional<User> updatedOpt = userRepository.updateUser(userToUpdate);
        return UserMapper.toUserUpdateDto(updatedOpt.get());
    }

    @Override
    public UserDto getUserById(Long userId) {
        if (!userRepository.isUserExists(userId)) {
            throw new UserDoesntExistException("пользователь с таким id не существует");
        }

        Optional<User> userOpt = userRepository.getUserById(userId);
        return UserMapper.toUserDto(userOpt.get());
    }

    @Override
    public UserDto deleteUserById(Long userId) {
        if (!userRepository.isUserExists(userId)) {
            throw new UserDoesntExistException("пользователь с таким id не существует");
        }

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
