package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    User createUser(User user);
    Optional<User> updateUser(User userToUpdate);
    Optional<User> getUserById(Long userId);
    Optional<User> deleteUserById(Long userId);
    Collection<User> getAllUsers();
    boolean isUserExists(long userId);
    boolean isEmailUnique(String email);
}
