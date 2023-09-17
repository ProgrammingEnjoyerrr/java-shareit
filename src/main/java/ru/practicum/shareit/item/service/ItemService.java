package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);
    ItemUpdateDto updateItem(Long userId, Long itemId, ItemUpdateDto itemUpdateDto);
    ItemDto getItemById(Long userId, Long ItemId);
    Collection<ItemDto> getAllUserItems(Long userId);
    Collection<ItemDto> getAvailableItemsByKeyWord(Long userId, String keyWord);
}
