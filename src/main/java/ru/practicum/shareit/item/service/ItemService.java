package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemUpdateDto updateItem(Long userId, Long itemId, ItemUpdateDto itemUpdateDto);

    ItemWithBookingDto getItemById(Long userId, Long itemId);

    Collection<ItemDto> getAllUserItems(Long userId);

    Collection<ItemDto> getAvailableItemsByKeyWord(Long userId, String keyWord);
}
