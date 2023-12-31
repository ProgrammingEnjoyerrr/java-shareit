package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("IntegrationTest")
@Disabled
public class UserServiceIT {

    @Autowired
    private UserService userService;

    private final UserDto userDto = UserDto.builder()
            .name("name")
            .email("example@email.com")
            .build();

    @Test
    @Order(1)
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void createUser() {
        UserDto created = userService.createUser(userDto);

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo(userDto.getName());
        assertThat(created.getEmail()).isEqualTo(userDto.getEmail());
    }

    @Test
    @Order(2)
    void getUserById_returnValidUser() {
        UserDto found = userService.getUserById(1L);

        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getName()).isEqualTo(userDto.getName());
        assertThat(found.getEmail()).isEqualTo(userDto.getEmail());
    }

    @Test
    @Order(3)
    void deleteUser() {
        userService.deleteUserById(1L);
        assertThat(true).isTrue();
    }

    @Test
    @Order(4)
    void getUserById_whenInvalidId_throwsUserNotFoundException() {
        long id = 1;

        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userService.getUserById(id))
                .withMessage("пользователь с id " + id + " не существует");
    }
}
