package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.common.BaseErrorHandler;
import ru.practicum.shareit.common.ErrorResponse;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.UserIsNotOwnerException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

@RestControllerAdvice(value = "ru.practicum.shareit.item.controller")
@Slf4j
public class ItemErrorHandler extends BaseErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleItemNotFoundException(final ItemNotFoundException e) {
        return commonErrorResponse(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(final UserNotFoundException e) {
        return commonErrorResponse(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUserIsNotOwnerException(final UserIsNotOwnerException e) {
        return commonErrorResponse(e);
    }
}
