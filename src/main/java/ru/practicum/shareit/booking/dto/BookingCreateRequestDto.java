package ru.practicum.shareit.booking.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
public class BookingCreateRequestDto {
    private Long itemId;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;
}
