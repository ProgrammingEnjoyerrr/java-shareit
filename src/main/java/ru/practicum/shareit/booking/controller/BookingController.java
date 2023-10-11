package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingCreateResponseDto;
import ru.practicum.shareit.booking.exception.BookingStateConversionException;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final String USER_ID_HEADER_LOG_PLACEHOLDER =
            USER_ID_HEADER + ": {}";

    private final BookingService bookingService;

    @PostMapping
    public BookingCreateResponseDto addBooking(@RequestHeader(value = USER_ID_HEADER) Long userId,
                                               @RequestBody @Valid BookingCreateRequestDto bookingCreateRequestDto) {
        log.info("got request POST /bookings");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, userId);
        log.info("request body: {}", bookingCreateRequestDto);

        return bookingService.addBooking(userId, bookingCreateRequestDto);
    }

    @PatchMapping(value = "/{bookingId}")
    public BookingCreateResponseDto refineBooking(@RequestHeader(value = USER_ID_HEADER) Long userId,
                                                  @PathVariable("bookingId") Long bookingId,
                                                  @RequestParam(name = "approved") Boolean approved) {
        log.info("got request PATCH /bookings/{bookingId}?approved={}", approved);
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, userId);
        log.info("bookingId = {}, approved = {}", bookingId, approved);

        return bookingService.refineBooking(userId, bookingId, approved);
    }

    @GetMapping(value = "/{bookingId}")
    public BookingCreateResponseDto getBookingData(@RequestHeader(value = USER_ID_HEADER) Long userId,
                                                   @PathVariable("bookingId") Long bookingId) {
        log.info("got request GET /bookings/{bookingId}");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, userId);
        log.info("bookingId = {}", bookingId);

        return bookingService.getBookingData(userId, bookingId);
    }

    @GetMapping
    public Collection<BookingCreateResponseDto> findAllBookingsForBooker(@RequestHeader(value = USER_ID_HEADER) Long userId,
                                                                         @RequestParam(name = "state", defaultValue = "ALL") String stateStr) {
        log.info("got request GET /bookings?state={state}");
        log.info("state = {}", stateStr);
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, userId);

        try {
            BookingState state = BookingState.valueOf(stateStr);
            return bookingService.findAllBookingsForBooker(userId, state);
        } catch (IllegalArgumentException e) {
            String message = "Unknown state: UNSUPPORTED_STATUS";
            log.error(message);
            throw new BookingStateConversionException(message);
        }
    }

    @GetMapping(path = "/owner")
    public Collection<BookingCreateResponseDto> findAllBookingsForItemsOwner(@RequestHeader(value = USER_ID_HEADER) Long ownerId,
                                                                             @RequestParam(name = "state", defaultValue = "ALL") String stateStr) {
        log.info("got request GET /bookings/owner?state={state}");
        log.info("state = {}", stateStr);
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, ownerId);

        try {
            BookingState state = BookingState.valueOf(stateStr);
            return bookingService.findAllBookingsForItemsOwner(ownerId, state);
        } catch (IllegalArgumentException e) {
            String message = "Unknown state: UNSUPPORTED_STATUS";
            log.error(message);
            throw new BookingStateConversionException(message);
        }
    }
}
