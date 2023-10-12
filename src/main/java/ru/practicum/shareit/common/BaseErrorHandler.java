package ru.practicum.shareit.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseErrorHandler {
    protected ErrorResponse commonErrorResponse(final Throwable e, final HttpStatus status) {
        logError(e, status);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        return new ErrorResponse(sStackTrace);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private ErrorResponse handleThrowable(final Throwable e) {
        logError(e, HttpStatus.INTERNAL_SERVER_ERROR);
        return new ErrorResponse(String.format("Произошла непредвиденная ошибка: %s.", e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private ValidationErrorResponse handleConstraintValidationException(ConstraintViolationException e) {
        logError(e, HttpStatus.BAD_REQUEST);

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
        logError(e, HttpStatus.BAD_REQUEST);

        final List<ValidationErrorResponse.Violation> violations = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorResponse.Violation(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ValidationErrorResponse(violations);
    }

    private void logError(final Throwable e, final HttpStatus status) {
        log.error("{} {}", status.value(), status.getReasonPhrase());
        log.error("thrown {} : {}", e.getClass().getCanonicalName(), e.getMessage());
        log.error("stacktrace: {}", Arrays.asList(e.getStackTrace()));
    }
}
