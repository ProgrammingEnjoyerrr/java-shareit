package ru.practicum.shareit.item.exception;

public class BookingInFutureException extends RuntimeException {
    public BookingInFutureException(final String message) {
        super(message);
    }
}
