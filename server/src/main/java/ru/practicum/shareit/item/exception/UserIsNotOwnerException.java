package ru.practicum.shareit.item.exception;

public class UserIsNotOwnerException extends RuntimeException {
    public UserIsNotOwnerException(String message) {
        super(message);
    }
}
