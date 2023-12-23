package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemUpdateDto);

    ItemDtoWithBooking getItemById(Long userId, Long itemId);

    Collection<ItemDtoWithBooking> getAllUserItems(Long userId);

    Collection<ItemDto> getAvailableItemsByKeyWord(Long userId, String keyWord);

    CommentDtoResponse addComment(Long userId, Long itemId, CommentDto commentDto);
}
