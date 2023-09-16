package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class ItemDto {
    private final String name;
    private final String description;
    private final boolean available;
}
