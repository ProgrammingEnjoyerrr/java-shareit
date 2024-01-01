package ru.practicum.shareit.booking.exception;

public class BookingAlreadyRefinedException extends RuntimeException {
    public BookingAlreadyRefinedException(final String message) {
        super(message);
    }
}
