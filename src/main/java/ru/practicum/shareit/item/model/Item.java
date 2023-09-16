package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class Item {
    private final Integer id;
    private final String name;
    private final String description;
    private final boolean available;

}
