package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingCreateResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;

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
    public BookingCreateResponseDto refineBooking(@PathVariable("bookingId") Long bookingId,
                              @RequestParam(name = "approved") Boolean approved) {
        log.info("got request PATCH /bookings/{bookingId}?approved={}", approved);
        log.info("bookingId = {}, approved = {}", bookingId, approved);

        return bookingService.refineBooking(bookingId, approved);
    }
}
