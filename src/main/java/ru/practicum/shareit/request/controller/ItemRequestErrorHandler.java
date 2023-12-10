package ru.practicum.shareit.request.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.common.BaseErrorHandler;
import ru.practicum.shareit.common.ErrorResponse;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;

@RestControllerAdvice(value = "ru.practicum.shareit.request.controller")
public class ItemRequestErrorHandler extends BaseErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleItemRequestNotFoundException(final ItemRequestNotFoundException e) {
        return commonErrorResponse(e, HttpStatus.NOT_FOUND);
    }
}
