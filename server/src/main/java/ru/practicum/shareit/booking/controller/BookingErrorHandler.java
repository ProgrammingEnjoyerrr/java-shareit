package ru.practicum.shareit.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exception.*;
import ru.practicum.shareit.common.BaseErrorHandler;
import ru.practicum.shareit.common.ErrorResponse;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

@RestControllerAdvice(value = "ru.practicum.shareit.booking.controller")
public class BookingErrorHandler extends BaseErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleItemIsUnavailableException(final ItemIsUnavailableException e) {
        return commonErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(final UserNotFoundException e) {
        return commonErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleItemNotFoundException(final ItemNotFoundException e) {
        return commonErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingDatesValidatorException(final BookingDatesValidatorException e) {
        return commonErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleBookingNotFoundException(final BookingNotFoundException e) {
        return commonErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleForbiddenAccessException(final ForbiddenAccessException e) {
        return commonErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingStateConversionException(final BookingStateConversionException e) {
        return commonErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingAlreadyRefinedException(final BookingAlreadyRefinedException e) {
        return commonErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserIsNotOwnerException(final UserIsNotOwnerException e) {
        return commonErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserIsOwnerException(final UserIsOwnerException e) {
        return commonErrorResponse(e, HttpStatus.NOT_FOUND);
    }
}
