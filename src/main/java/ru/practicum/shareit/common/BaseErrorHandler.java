package ru.practicum.shareit.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseErrorHandler {
    private static final String LOG_ERROR_PLACEHOLDER = "error occurred: {}";

    protected ErrorResponse commonErrorResponse(final RuntimeException e) {
        log.error(LOG_ERROR_PLACEHOLDER, e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private ErrorResponse handleThrowable(final Throwable e) {
        log.error(LOG_ERROR_PLACEHOLDER, e.getMessage(), e);
        return new ErrorResponse(String.format("Произошла непредвиденная ошибка: %s.", e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private ValidationErrorResponse handleConstraintValidationException(ConstraintViolationException e) {
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
    private ValidationErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(LOG_ERROR_PLACEHOLDER, e.getMessage(), e);

        final List<ValidationErrorResponse.Violation> violations = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorResponse.Violation(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ValidationErrorResponse(violations);
    }
}
