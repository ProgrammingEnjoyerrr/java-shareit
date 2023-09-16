package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    User createUser(User userDto);
    Optional<User> updateUser(User userDto);
    Optional<User> getUserById(Long userId);
    Optional<User> deleteUserById(Long userId);
    Collection<User> getAllUsers();
}
