package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {
    Item createItem(Item item);
    Optional<Item> updateItem(Item itemToUpdate);
    Optional<Item> getItemById(long itemId);
    Collection<Item> getAllItems(Long userId);
    Collection<Item> getAvailableItemsByKeyWord(String keyWord);
    boolean isItemExists(long itemId);
}
