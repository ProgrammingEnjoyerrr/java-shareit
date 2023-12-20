package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    private final User user = User.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final User fakeUser = User.builder()
            .id(2L)
            .name("faker")
            .email("abrakadabra@email.ru")
            .build();

    private final ItemRequestRequestDto dto = ItemRequestRequestDto.builder()
            .description("description")
            .build();

    private final ItemRequest itemRequest =
            ItemRequestMapper.toItemRequest(dto, user);

    private final ItemRequestResponseDto responseDto =
            ItemRequestMapper.toItemRequestResponseDto(itemRequest);

    @Test
    @SneakyThrows
    void createItemRequest() {
        Long userId = user.getId();

        when(itemRequestService.createItemRequest(userId, dto)).thenReturn(responseDto);

        String response = mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(responseDto));

        verify(itemRequestService, times(1)).createItemRequest(userId, dto);
    }

    @Test
    @SneakyThrows
    void getUserItemRequests() {
        Long userId = user.getId();
        Collection<ItemRequest> itemRequests = List.of(itemRequest);
        Collection<ItemRequestResponseDto> expected = itemRequests.stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());

        when(itemRequestService.getUserItemRequests(userId)).thenReturn(expected);

        String response = mockMvc.perform(get("/requests")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(expected));

        verify(itemRequestService, times(1)).getUserItemRequests(userId);
    }

    @Test
    @SneakyThrows
    void getItemRequestFromOtherUsers() {
        Long userId = fakeUser.getId();
        Integer from = 0;
        Integer size = 10;

        Collection<ItemRequest> itemRequests = List.of(itemRequest);
        Collection<ItemRequestResponseDto> expected = itemRequests.stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());

        when(itemRequestService.getItemRequestFromOtherUsers(userId, from, size))
                .thenReturn(expected);

        String response = mockMvc.perform(get("/requests/all")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("to", String.valueOf(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(expected));

        verify(itemRequestService, times(1)).getItemRequestFromOtherUsers(userId, from, size);
    }

    @Test
    @SneakyThrows
    void getItemRequestById() {
        Long userId = user.getId();
        Long requestId = 100500L;
        ItemRequestResponseDto expected = ItemRequestMapper.toItemRequestResponseDto(itemRequest);

        when(itemRequestService.getItemRequestById(userId, requestId)).thenReturn(expected);

        String response = mockMvc.perform(get("/requests/{requestId}", requestId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(expected));

        verify(itemRequestService, times(1)).getItemRequestById(userId, requestId);
    }

    @Test
    @SneakyThrows
    void getItemRequestById_whenUserNotFound_throwsUserNotFoundException() {
        Long userId = user.getId();
        Long requestId = 100500L;

        when(itemRequestService.getItemRequestById(userId, requestId))
                .thenThrow(UserNotFoundException.class);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1))
                .getItemRequestById(userId, requestId);
    }

    @Test
    @SneakyThrows
    void getItemRequestById_whenItemRequestNotFound_throwsItemRequestNotFoundException() {
        Long userId = user.getId();
        Long requestId = 100500L;

        when(itemRequestService.getItemRequestById(userId, requestId))
                .thenThrow(ItemRequestNotFoundException.class);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1))
                .getItemRequestById(userId, requestId);
    }
}