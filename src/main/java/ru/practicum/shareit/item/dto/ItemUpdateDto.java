package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@AllArgsConstructor
public class ItemUpdateDto {
    @Setter
    private Long id;
    private final String name;
    private final String description;
    private final boolean available;
}
