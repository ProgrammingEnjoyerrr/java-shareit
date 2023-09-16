package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.common.ErrorResponse;
import ru.practicum.shareit.common.ValidationErrorResponse;
import ru.practicum.shareit.user.exception.NonUniqueEmailException;
import ru.practicum.shareit.user.exception.UserDoesntExistException;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice(value = "ru.practicum.shareit.user.controller")
@Slf4j
public class UserErrorHandler {

    public static final String LOG_ERROR_PLACEHOLDER = "error occurred: {}";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleNonUniqueEmailException(final NonUniqueEmailException e) {
        return commonErrorResponse(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserDoesntExistException(final UserDoesntExistException e) {
        return commonErrorResponse(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.error(LOG_ERROR_PLACEHOLDER, e.getMessage(), e);
        return new ErrorResponse(String.format("Произошла непредвиденная ошибка: %s.", e.getMessage()));
    }

    private ErrorResponse commonErrorResponse(final RuntimeException e) {
        log.error(LOG_ERROR_PLACEHOLDER, e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleConstraintValidationException(ConstraintViolationException e) {
        log.error(LOG_ERROR_PLACEHOLDER, e.getMessage(), e);

        final List<ValidationErrorResponse.Violation> violations = e.getConstraintViolations()
                .stream()
                .map(violation -> new ValidationErrorResponse.Violation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage())
                )
                .collect(Collectors.toList());

        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(LOG_ERROR_PLACEHOLDER, e.getMessage(), e);

        final List<ValidationErrorResponse.Violation> violations = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorResponse.Violation(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ValidationErrorResponse(violations);
    }
}
