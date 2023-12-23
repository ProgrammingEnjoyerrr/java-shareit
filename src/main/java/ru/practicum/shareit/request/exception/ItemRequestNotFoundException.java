package ru.practicum.shareit.request.exception;

public class ItemRequestNotFoundException extends RuntimeException {
    public ItemRequestNotFoundException(final String message) {
        super(message);
    }
}
