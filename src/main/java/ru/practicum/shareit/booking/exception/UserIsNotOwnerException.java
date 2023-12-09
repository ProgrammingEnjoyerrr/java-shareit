package ru.practicum.shareit.booking.exception;

public class UserIsNotOwnerException extends RuntimeException {
    public UserIsNotOwnerException(final String message) {
        super(message);
    }
}
