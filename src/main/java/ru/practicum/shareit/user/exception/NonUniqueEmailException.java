package ru.practicum.shareit.user.exception;

public class NonUniqueEmailException extends RuntimeException {
    public NonUniqueEmailException(String message) {
        super(message);
    }
}
