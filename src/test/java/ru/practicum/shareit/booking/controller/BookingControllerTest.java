package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingCreateResponseDto;
import ru.practicum.shareit.booking.exception.*;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.controller.BookingController.USER_ID_HEADER;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private final User user = User.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final Item item = Item.builder()
            .id(1L)
            .name("item name")
            .description("description")
            .owner(user)
            .build();

    private final BookingCreateRequestDto bookingDto = BookingCreateRequestDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .build();

    private final BookingCreateResponseDto bookingDtoOut = BookingCreateResponseDto.builder()
            .id(1L)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .status(BookingStatus.WAITING)
            .booker(user)
            .item(item)
            .build();


    @Test
    @SneakyThrows
    void addBooking() {
        when(bookingService.addBooking(user.getId(), bookingDto)).thenReturn(bookingDtoOut);

        String result = mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDtoOut), result);
    }

    @Test
    @SneakyThrows
    void addBooking_whenUserNotFound_throwsUserNotFoundException() {
        when(bookingService.addBooking(anyLong(), any())).thenThrow(UserNotFoundException.class);

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());
        verify(bookingService, only()).addBooking(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void addBooking_whenItemNotFound_throwsItemNotFoundException() {
        when(bookingService.addBooking(anyLong(), any())).thenThrow(ItemNotFoundException.class);

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());
        verify(bookingService, only()).addBooking(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void addBooking_whenItemIsUnavailable_throwsItemIsUnavailableException() {
        when(bookingService.addBooking(anyLong(), any())).thenThrow(ItemIsUnavailableException.class);

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
        verify(bookingService, only()).addBooking(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void addBooking_whenUserIsOwner_throwsUserIsOwnerException() {
        when(bookingService.addBooking(anyLong(), any())).thenThrow(UserIsOwnerException.class);

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());
        verify(bookingService, only()).addBooking(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void addBooking_whenBookingDatesValidatorException_throwsBookingDatesValidatorException() {
        when(bookingService.addBooking(anyLong(), any())).thenThrow(BookingDatesValidatorException.class);

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
        verify(bookingService, only()).addBooking(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void refineBooking() {
        Boolean approved = true;
        Long bookingId = 1L;

        when(bookingService.refineBooking(user.getId(), bookingId, approved)).thenReturn(bookingDtoOut);

        String result = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDtoOut), result);
    }

    @Test
    @SneakyThrows
    void refineBooking_whenNotBooked_throwsBookingNotFoundException() {
        Boolean approved = true;
        Long bookingId = 1L;

        when(bookingService.refineBooking(user.getId(), bookingId, approved)).thenThrow(BookingNotFoundException.class);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isNotFound());

        verify(bookingService, only()).refineBooking(anyLong(), any(), any());
    }

    @Test
    @SneakyThrows
    void refineBooking_whenBookingStatusIsApprovedOrRejected_throwsBookingAlreadyRefinedException() {
        Boolean approved = true;
        Long bookingId = 1L;

        when(bookingService.refineBooking(user.getId(), bookingId, approved)).thenThrow(BookingAlreadyRefinedException.class);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isBadRequest());

        verify(bookingService, only()).refineBooking(anyLong(), any(), any());
    }

    @Test
    @SneakyThrows
    void refineBooking_whenUserIsNotOwner_throwsUserIsNotOwnerException() {
        Boolean approved = true;
        Long bookingId = 1L;

        when(bookingService.refineBooking(user.getId(), bookingId, approved)).thenThrow(UserIsNotOwnerException.class);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header(USER_ID_HEADER, user.getId())
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isNotFound());

        verify(bookingService, only()).refineBooking(anyLong(), any(), any());
    }

    @Test
    @SneakyThrows
    void getBookingData() {
        Long bookingId = 1L;

        when(bookingService.getBookingData(user.getId(), bookingId)).thenReturn(bookingDtoOut);

        String result = mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDtoOut), result);
    }

    @Test
    @SneakyThrows
    void getBookingData_whenUserIsNotBookerAndUserIsNotOwner_throwsForbiddenAccessException() {
        Long bookingId = 1L;

        when(bookingService.getBookingData(user.getId(), bookingId)).thenThrow(ForbiddenAccessException.class);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isNotFound());

        verify(bookingService, only()).getBookingData(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void findAllBookingsForBooker() {
        Integer from = 0;
        Integer size = 10;
        String state = "ALL";

        when(bookingService.findAllBookingsForBooker(user.getId(), BookingState.ALL.toString(), 0, 10))
                .thenReturn(List.of(bookingDtoOut));

        String result = mockMvc.perform(get("/bookings")
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(bookingDtoOut)), result);
    }

    @Test
    @SneakyThrows
    void findAllBookingsForBooker_whenWrongStringState_throwsBookingStateConversionException() {
        Integer from = 0;
        Integer size = 10;
        String state = "ALL";

        when(bookingService.findAllBookingsForBooker(user.getId(), BookingState.ALL.toString(), 0, 10))
                .thenThrow(BookingStateConversionException.class);

        mockMvc.perform(get("/bookings")
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, only()).findAllBookingsForBooker(anyLong(), any(), any(), any());
    }

    @Test
    @SneakyThrows
    void findAllBookingsForItemsOwner() {
        Integer from = 0;
        Integer size = 10;
        String state = "ALL";

        when(bookingService.findAllBookingsForItemsOwner(user.getId(), BookingState.ALL.toString(), 0, 10))
                .thenReturn(List.of(bookingDtoOut));

        String result = mockMvc.perform(get("/bookings/owner")
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(bookingDtoOut)), result);
    }
}