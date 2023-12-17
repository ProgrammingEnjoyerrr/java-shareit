package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingCreateResponseDto;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("IntegrationTest")
public class BookingServiceIT {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private final UserDto userDto1 = UserDto.builder()
            .name("name1")
            .email("email1@email.com")
            .build();

    private final UserDto userDto2 = UserDto.builder()
            .name("name2")
            .email("email2@email.com")
            .build();

    private final ItemDto itemDto1 = ItemDto.builder()
            .name("item1 name")
            .description("item1 description")
            .available(true)
            .build();

    private final ItemDto itemDto2 = ItemDto.builder()
            .name("item2 name")
            .description("item2 description")
            .available(true)
            .build();

    private final BookingCreateRequestDto bookingDto1 = BookingCreateRequestDto.builder()
            .itemId(2L)
            .start(LocalDateTime.now().plusSeconds(10L))
            .end(LocalDateTime.now().plusSeconds(11L))
            .build();

    @Test
    @Order(1)
    void addBooking() {
        UserDto addedUser1 = userService.createUser(userDto1);
        UserDto addedUser2 = userService.createUser(userDto2);
        itemService.createItem(addedUser1.getId(), itemDto1);
        itemService.createItem(addedUser2.getId(), itemDto2);

        BookingCreateResponseDto bookingDtoOut1 = bookingService.addBooking(addedUser1.getId(), bookingDto1);
        BookingCreateResponseDto bookingDtoOut2 = bookingService.addBooking(addedUser1.getId(), bookingDto1);

        assertThat(bookingDtoOut1.getId()).isEqualTo(1L);
        assertThat(bookingDtoOut2.getId()).isEqualTo(2L);
        assertThat(bookingDtoOut1.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookingDtoOut2.getStatus()).isEqualTo(BookingStatus.WAITING);

        BookingCreateResponseDto updatedBookingCreateRequestDto1 = bookingService.refineBooking(addedUser2.getId(),
                bookingDtoOut1.getId(), true);
        BookingCreateResponseDto updatedBookingCreateRequestDto2 = bookingService.refineBooking(addedUser2.getId(),
                bookingDtoOut2.getId(), true);

        assertThat(updatedBookingCreateRequestDto1.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(updatedBookingCreateRequestDto2.getStatus()).isEqualTo(BookingStatus.APPROVED);

        Collection<BookingCreateResponseDto> bookingsDtoOut = bookingService.findAllBookingsForItemsOwner(
                addedUser2.getId(), BookingState.ALL.toString(), 0, 10);

        assertThat(bookingsDtoOut).hasSize(2);
    }

    @Test
    @Order(2)
    void refineBooking_whenBookingIdAndUserIdIsNotValid_thenThrowBookingNotFoundException() {
        Long userId = 3L;
        Long bookingId = 3L;

        assertThatExceptionOfType(BookingNotFoundException.class)
                .isThrownBy(() -> bookingService.refineBooking(userId, bookingId, true))
                .withMessage("бронирования с id " + bookingId + " не существует");
    }
}
