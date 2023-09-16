package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    @Override
    public User createUser(User userDto) {
        return null;
    }

    @Override
    public Optional<User> updateUser(User userDto) {
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.empty();
    }

    @Override
    public Optional<User> deleteUserById(Long userId) {
        return Optional.empty();
    }

    @Override
    public Collection<User> getAllUsers() {
        return null;
    }
}
