package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {
    User createUser(User user);

    User updateUser(User userToUpdate);

    User getUserById(Long userId);

    User deleteUserById(Long userId);

    Collection<User> getAllUsers();

    boolean isUserExists(long userId);

    boolean isEmailUnique(String email, Long userId);
}
