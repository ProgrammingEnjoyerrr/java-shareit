package ru.practicum.shareit.booking.exception;

public class ItemIsUnavailableException extends RuntimeException {
    public ItemIsUnavailableException(String message) {
        super(message);
    }
}
