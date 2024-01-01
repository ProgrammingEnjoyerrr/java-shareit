package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.UserIsNotBookerException;
import ru.practicum.shareit.item.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemDto itemDto = ItemDto.builder()
            .id(100L)
            .name("name")
            .description("description")
            .available(true)
            .build();
    private final User owner = User.builder()
            .id(200L)
            .name("name")
            .email("example@email.com")
            .build();
    private final Item item = ItemMapper.toItem(itemDto, owner);
    private final User user = User.builder()
            .id(2000L)
            .name("user_name")
            .email("exampleEmail@email.com")
            .build();

    private final Comment comment1 = Comment.builder()
            .id(1000L)
            .text("text1")
            .item(item)
            .author(user)
            .created(LocalDateTime.now())
            .build();

    private final Comment comment2 = Comment.builder()
            .id(1001L)
            .text("text2")
            .item(item)
            .author(user)
            .created(LocalDateTime.now())
            .build();

    private final Booking booking1 = Booking.builder()
            .id(3000L)
            .startDate(LocalDateTime.now().minusHours(2))
            .endDate(LocalDateTime.now().minusHours(1))
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .build();

    private final Booking booking2 = Booking.builder()
            .id(3001L)
            .startDate(LocalDateTime.now().plusHours(1))
            .endDate(LocalDateTime.now().plusHours(2))
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .build();

    @Test
    @SneakyThrows
    void createItem() {
        Long userId = item.getOwner().getId();

        when(itemService.createItem(userId, itemDto)).thenReturn(itemDto);

        String response = mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(itemDto));
        verify(itemService, times(1)).createItem(userId, itemDto);
    }

    @Test
    @SneakyThrows
    void updateItem() {
        Long userId = item.getOwner().getId();
        Long itemId = item.getId();

        when(itemService.updateItem(userId, itemId, itemDto)).thenReturn(itemDto);

        String response = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(itemDto));
        verify(itemService, times(1)).updateItem(userId, itemId, itemDto);
    }

    @Test
    @SneakyThrows
    void updateItem_whenItemNotFound_throwsItemNotFoundException() {
        Long userId = item.getOwner().getId();
        Long itemId = item.getId();

        when(itemService.updateItem(userId, itemId, itemDto))
                .thenThrow(ItemNotFoundException.class);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).updateItem(userId, itemId, itemDto);
    }

    @Test
    @SneakyThrows
    void updateItem_whenUserNotFound_throwsUserNotFoundException() {
        Long userId = item.getOwner().getId();
        Long itemId = item.getId();

        when(itemService.updateItem(userId, itemId, itemDto))
                .thenThrow(UserNotFoundException.class);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).updateItem(userId, itemId, itemDto);
    }

    @Test
    @SneakyThrows
    void updateItem_whenUserIsNotOwner_throwsUserIsNotOwnerException() {
        Long userId = user.getId();
        Long itemId = item.getId();

        when(itemService.updateItem(userId, itemId, itemDto))
                .thenThrow(UserIsNotOwnerException.class);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden());

        verify(itemService, times(1)).updateItem(userId, itemId, itemDto);
    }

    @Test
    @SneakyThrows
    void getItemByUserId() {
        Long userId = item.getOwner().getId();
        Long itemId = item.getId();

        ItemDtoWithBooking itemDtoWithBooking = ItemMapper.toItemDtoWithBooking(
                item, List.of(comment1, comment2), booking1, booking2
        );

        when(itemService.getItemById(userId, itemId)).thenReturn(itemDtoWithBooking);

        String response = mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(itemDtoWithBooking));
        verify(itemService, times(1)).getItemById(userId, itemId);
    }

    @Test
    @SneakyThrows
    void getAllUserItems() {
        Long userId = item.getOwner().getId();

        ItemDtoWithBooking itemDtoWithBooking = ItemMapper.toItemDtoWithBooking(
                item, List.of(comment1, comment2), booking1, booking2
        );

        when(itemService.getAllUserItems(userId)).thenReturn(List.of(itemDtoWithBooking));

        String response = mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(List.of(itemDtoWithBooking)));
        verify(itemService, times(1)).getAllUserItems(userId);
    }

    @Test
    @SneakyThrows
    void getAvailableItemsByKeyWord() {
        Long userId = item.getOwner().getId();
        String keyWord = "keyw";

        when(itemService.getAvailableItemsByKeyWord(userId, keyWord))
                .thenReturn(List.of(itemDto));

        String response = mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, userId)
                        .param("text", keyWord))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(List.of(itemDto)));
        verify(itemService, times(1))
                .getAvailableItemsByKeyWord(userId, keyWord);
    }

    @Test
    @SneakyThrows
    void addComment() {
        Long userId = item.getOwner().getId();
        Long itemId = item.getId();

        CommentDto dto = CommentMapper.toCommentDto(comment1);
        CommentDtoResponse responseDto = CommentMapper.toCommentResponse(comment1);

        when(itemService.addComment(userId, itemId, dto)).thenReturn(responseDto);

        String response = mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .isEqualTo(objectMapper.writeValueAsString(responseDto));
        verify(itemService, times(1)).addComment(userId, itemId, dto);
    }

    @Test
    @SneakyThrows
    void addComment_whenUserIsNotBooker_throwsUserIsNotBookerException() {
        Long userId = item.getOwner().getId();
        Long itemId = item.getId();

        CommentDto dto = CommentMapper.toCommentDto(comment1);

        when(itemService.addComment(userId, itemId, dto))
                .thenThrow(UserIsNotBookerException.class);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemService, times(1)).addComment(userId, itemId, dto);
    }
}