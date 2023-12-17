package ru.practicum.shareit.item.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class BookingMetaData {
    private Long id;
    private Long bookerId;
}
