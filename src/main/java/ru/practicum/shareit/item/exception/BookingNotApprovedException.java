package ru.practicum.shareit.item.exception;

public class BookingNotApprovedException extends RuntimeException {
    public BookingNotApprovedException(final String message) {
        super(message);
    }
}
