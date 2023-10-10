package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Long> {
//    User createUser(User user);
//
//    User updateUser(User userToUpdate);
//
//    User getUserById(Long userId);
//
//    User deleteById(Long id);
//
//    Collection<User> getAllUsers();
//
//    boolean isUserExists(long userId);
//
//    boolean isEmailUnique(String email, Long userId);
}
