package ru.practicum.shareit.item.services;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto);
    ItemDto updateItem(ItemDto itemDto);
    ItemDto getItemById(int id);
    Collection<ItemDto> getAllItems();
}
