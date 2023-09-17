package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@RequiredArgsConstructor
public class ItemDto {
    private final Long id;
    @NotNull
    private final String name;
    @NotNull
    private final String description;
    @NotNull
    private final boolean available;
}
