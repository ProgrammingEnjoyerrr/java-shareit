package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("name")
            .email("example@email.com")
            .build();

    @Test
    void createUser() {
        User userToSave = User.builder()
                .id(1L)
                .name("name")
                .email("example@email.com")
                .build();
        when(userRepository.save(any())).thenReturn(userToSave);

        UserDto actualUserDto = userService.createUser(userDto);

        assertThat(actualUserDto).isEqualTo(userDto);

        verify(userRepository, times(1)).save(userToSave);
    }

    @Test
    void updateUser_whenUserExists_returnOk() {
        User user = UserMapper.toUser(userDto);
        Long userId = user.getId();

        UserUpdateDto fieldsToUpdate = new UserUpdateDto(userId, "Updated User", "updated@example.com");
        User updatedUser = UserMapper.toUser(fieldsToUpdate);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserUpdateDto updatedUserDto = userService.updateUser(fieldsToUpdate);

        assertThat(updatedUserDto)
                .isNotNull()
                .extracting("name", "email")
                .containsExactly(fieldsToUpdate.getName(), fieldsToUpdate.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void updateUser_whenUserNotExists_throwUserNotFoundException() {
        UserUpdateDto userUpdateDto = new UserUpdateDto(0L, "Updated User", "updated@example.com");
        Long userId = userUpdateDto.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userService.updateUser(userUpdateDto))
                .withMessage("пользователь с id " + userId + " не существует");

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_whenUserExists_returnOk() {
        long userId = 1L;
        User expectedUser = User.builder()
                .id(userId)
                .name("name")
                .email("example@email.com")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        UserDto expectedUserDto = UserMapper.toUserDto(expectedUser);

        UserDto actualUserDto = userService.getUserById(userId);

        assertThat(actualUserDto).isEqualTo(expectedUserDto);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_whenUserNotExists_throwUserNotFoundException() {
        long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userService.getUserById(userId))
                .withMessage("пользователь с id " + userId + " не существует");

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void deleteUserById() {
        long userId = 0L;
        userService.deleteUserById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void getAllUsers() {
        List<User> expectedUsers = List.of(new User());
        List<UserDto> expectedUserDto = expectedUsers.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());

        when(userRepository.findAll()).thenReturn(expectedUsers);

        Collection<UserDto> actualUsersDto = userService.getAllUsers();

        assertThat(actualUsersDto)
                .hasSize(1)
                .usingRecursiveAssertion()
                .isEqualTo(expectedUserDto);

        verify(userRepository, times(1)).findAll();
    }
}