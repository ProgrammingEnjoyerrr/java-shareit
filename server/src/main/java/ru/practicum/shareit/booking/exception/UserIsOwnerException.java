package ru.practicum.shareit.booking.exception;

public class UserIsOwnerException extends RuntimeException {
    public UserIsOwnerException(final String message) {
        super(message);
    }
}
