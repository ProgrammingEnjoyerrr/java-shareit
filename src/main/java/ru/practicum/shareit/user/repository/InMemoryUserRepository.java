package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long id = 0;

    @Override
    public User createUser(User user) {
        ++id;
        User newUser = User.builder()
                .id(id)
                .name(user.getName())
                .email(user.getEmail())
                .build();
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public Optional<User> updateUser(User userToUpdate) {
        User oldUser = users.get(userToUpdate.getId());

        User updatedUser = User.builder()
                .id(oldUser.getId())
                .name(userToUpdate.getName() != null ? userToUpdate.getName() : oldUser.getName())
                .email(userToUpdate.getEmail() != null ? userToUpdate.getEmail() : oldUser.getEmail())
                .build();

        users.put(oldUser.getId(), updatedUser);

        return Optional.of(updatedUser);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.of(users.get(userId));
    }

    @Override
    public Optional<User> deleteUserById(Long userId) {
        User removed = users.remove(userId);
        return Optional.of(removed);
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public boolean isUserExists(long userId) {
        return users.containsKey(userId);
    }

    @Override
    public boolean isEmailUnique(String email) {
        Optional<User> found = users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findAny();
        return found.isEmpty();
    }
}
