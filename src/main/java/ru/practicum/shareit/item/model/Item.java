package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class Item {
    private final Long id;
    private final String name;
    private final String description;
    private final Boolean available;
}
