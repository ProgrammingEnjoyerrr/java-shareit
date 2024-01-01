package ru.practicum.shareit.item.exception;

public class UserIsNotBookerException extends RuntimeException {
    public UserIsNotBookerException(String message) {
        super(message);
    }
}
