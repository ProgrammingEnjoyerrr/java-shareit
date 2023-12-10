package ru.practicum.shareit.request.exception;

public class WrongPaginationParameterException extends RuntimeException {
    public WrongPaginationParameterException(final String message) {
        super(message);
    }
}
