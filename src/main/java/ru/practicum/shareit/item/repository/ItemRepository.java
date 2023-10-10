package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Item createItem(Item item);

    Item updateItem(Item itemToUpdate);

    Item getItemById(Long itemId);

    Collection<Item> getAllItems(Long userId);

    Collection<Item> getAvailableItemsByKeyWord(String keyWord);

    boolean isItemExists(Long itemId);
}
