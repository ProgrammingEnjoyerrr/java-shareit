package ru.practicum.shareit.booking.exception;

public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException(final String message) {
        super(message);
    }
}
