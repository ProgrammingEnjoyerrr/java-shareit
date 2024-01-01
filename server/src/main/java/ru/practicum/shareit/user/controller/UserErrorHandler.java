package ru.practicum.shareit.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.common.BaseErrorHandler;
import ru.practicum.shareit.common.ErrorResponse;
import ru.practicum.shareit.user.exception.UserNotFoundException;

@RestControllerAdvice(value = "ru.practicum.shareit.user.controller")
public class UserErrorHandler extends BaseErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(final UserNotFoundException e) {
        return commonErrorResponse(e, HttpStatus.NOT_FOUND);
    }
}
