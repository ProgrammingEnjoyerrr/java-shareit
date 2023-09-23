package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long itemId = 0;

    @Override
    public Item createItem(Item item) {
        ++itemId;

        Item newItem = Item.builder()
                .id(itemId)
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwnerId())
                .build();

        items.put(newItem.getId(), newItem);

        return newItem;
    }

    @Override
    public Item updateItem(Item itemToUpdate) {
        Item oldItem = items.get(itemToUpdate.getId());

        Item updatedItem = Item.builder()
                .id(oldItem.getId())
                .name(itemToUpdate.getName() != null ? itemToUpdate.getName() : oldItem.getName())
                .description(itemToUpdate.getDescription() != null ? itemToUpdate.getDescription() : oldItem.getDescription())
                .available(itemToUpdate.getAvailable() != null ? itemToUpdate.getAvailable() : oldItem.getAvailable())
                .ownerId(oldItem.getOwnerId())
                .build();

        items.put(oldItem.getId(), updatedItem);

        return updatedItem;
    }

    @Override
    public Item getItemById(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public Collection<Item> getAllItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> getAvailableItemsByKeyWord(String keyWord) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> containsIgnoreCase(item.getName(), keyWord) ||
                        containsIgnoreCase(item.getDescription(), keyWord))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isItemExists(Long itemId) {
        return items.containsKey(itemId);
    }

    private boolean containsIgnoreCase(String lhs, String rhs) {
        return lhs.toLowerCase().contains(rhs.toLowerCase());
    }
}
