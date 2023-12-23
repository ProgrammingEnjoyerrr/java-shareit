package ru.practicum.shareit.booking.service;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.exception.BookingDatesValidatorException;

import java.time.LocalDateTime;


@UtilityClass
public class BookingDatesValidator {

    public void validate(BookingCreateRequestDto bookingCreateRequestDto) {
        LocalDateTime start = bookingCreateRequestDto.getStart();
        LocalDateTime end = bookingCreateRequestDto.getEnd();
        LocalDateTime now = LocalDateTime.now();

        if (start.isBefore(now)) {
            throw new BookingDatesValidatorException(
                    "дата начала не может находиться в прошлом");
        }

        if (end.isBefore(now)) {
            throw new BookingDatesValidatorException(
                    "дата окончания не может быть в прошлом");
        }

        if (end.isBefore(start)) {
            throw new BookingDatesValidatorException(
                    "дата окончания не может быть перед датой начала");
        }

        if (end.isEqual(start)) {
            throw new BookingDatesValidatorException(
                    "дата окончания должна отличаться от даты начала");
        }
    }
}
