package ru.practicum.shareit.item.dto;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@ToString
public class ItemUpdateDto {
    @Setter
    private Long id;
    private final String name;
    private final String description;
    private final Boolean available;
}
