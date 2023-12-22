package ru.practicum.shareit.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@UtilityClass
public class ItemRequestMapper {
    public ItemRequestResponseDto
    toItemRequestResponseDto(ItemRequest itemRequest) {
        List<ItemDto> items = new ArrayList<>();
        if (Objects.nonNull(itemRequest.getItems())) {
            items = itemRequest.getItems()
                    .stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }

        return ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }

    public ItemRequest toItemRequest(ItemRequestRequestDto dto, User requester) {
        return ItemRequest.builder()
                .requester(requester)
                .description(dto.getDescription())
                .created(LocalDateTime.now())
                .build();
    }
}
