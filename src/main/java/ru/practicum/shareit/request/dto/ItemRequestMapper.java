package ru.practicum.shareit.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;

@UtilityClass
public class ItemRequestMapper {
    public ItemRequestResponseDto toItemResponseDto(ItemRequest itemRequest) {
        return ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }
}
