package ru.practicum.shareit.booking.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateRequestDtoTest {
    @Autowired
    private JacksonTester<BookingCreateRequestDto> json;

    private static final String DATE_TIME = "2023-07-23T07:33:00";
    private static final Pattern PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$");


    private static final BookingCreateRequestDto BOOKING_CREATE_REQUEST_DTO = BookingCreateRequestDto.builder()
            .itemId(1L)
            .start(LocalDateTime.parse(DATE_TIME))
            .end(LocalDateTime.parse(DATE_TIME))
            .build();

    @Test
    @SneakyThrows
    public void startSerializes() {
        assertThat(json.write(BOOKING_CREATE_REQUEST_DTO))
                .extractingJsonPathStringValue("$.start")
                .matches(PATTERN)
                .isEqualTo(DATE_TIME);
    }

    @Test
    @SneakyThrows
    public void endSerializes() {
        assertThat(json.write(BOOKING_CREATE_REQUEST_DTO))
                .extractingJsonPathStringValue("$.end")
                .matches(PATTERN)
                .isEqualTo(DATE_TIME);
    }
}