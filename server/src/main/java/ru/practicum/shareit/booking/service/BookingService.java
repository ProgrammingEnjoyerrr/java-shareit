package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingCreateResponseDto;

import java.util.Collection;

public interface BookingService {
    BookingCreateResponseDto addBooking(Long userId, BookingCreateRequestDto bookingCreateRequestDto);

    BookingCreateResponseDto refineBooking(Long userId, Long bookingId, Boolean approved);

    BookingCreateResponseDto getBookingData(Long userId, Long bookingId);

    Collection<BookingCreateResponseDto> findAllBookingsForBooker(Long bookerId, String stateStr, Integer from, Integer size);

    Collection<BookingCreateResponseDto> findAllBookingsForItemsOwner(Long ownerId, String stateStr, Integer from, Integer size);
}
