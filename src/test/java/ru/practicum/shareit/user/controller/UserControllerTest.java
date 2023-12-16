package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mvc;

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("name")
            .email("example@email.com")
            .build();

    @Test
    @SneakyThrows
    void createUser() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email("example@email.com")
                .build();

        when(userService.createUser(userDto)).thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).createUser(userDto);
    }

    @Test
    @SneakyThrows
    void updateUser() {
        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .id(1L)
                .name("name")
                .email("example@email.com")
                .build();

        when(userService.updateUser(userUpdateDto)).thenReturn(userUpdateDto);

        mvc.perform(patch("/users/{userId}", userUpdateDto.getId())
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userUpdateDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userUpdateDto.getName())))
                .andExpect(jsonPath("$.email", is(userUpdateDto.getEmail())));

        verify(userService, times(1)).updateUser(userUpdateDto);
    }

    @Test
    @SneakyThrows
    void getUserById() {
        long userId = userDto.getId();

        when(userService.getUserById(userId)).thenReturn(userDto);

        mvc.perform(get("/users/{userId}", userId)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @SneakyThrows
    void deleteUserById() {
        long userId = 1;

        mvc.perform(delete("/users/{userId}", userId)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUserById(userId);
    }

    @Test
    @SneakyThrows
    void getAllUsers() {
        List<UserDto> users = List.of(userDto);

        when(userService.getAllUsers()).thenReturn(users);

        String result = mvc.perform(get("/users")
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(result)
                .isEqualTo(mapper.writeValueAsString(users));

        verify(userService, times(1)).getAllUsers();
    }
}