package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
    public Optional<Item> updateItem(Item itemToUpdate) {
        Item oldItem = items.get(itemToUpdate.getId());

        Item updatedItem = Item.builder()
                .id(oldItem.getId())
                .name(itemToUpdate.getName() != null ? itemToUpdate.getName() : oldItem.getName())
                .description(itemToUpdate.getDescription() != null ? itemToUpdate.getDescription() : oldItem.getDescription())
                .available(itemToUpdate.getAvailable() != null ? itemToUpdate.getAvailable() : oldItem.getAvailable())
                .ownerId(oldItem.getOwnerId())
                .build();

        items.put(oldItem.getId(), updatedItem);

        return Optional.of(updatedItem);
    }

    @Override
    public Optional<Item> getItemById(long itemId) {
        return Optional.of(items.get(itemId));
    }

    @Override
    public Collection<Item> getAllItems() {
        return items.values();
    }

    @Override
    public Collection<Item> getAvailableItemsByKeyWord(String keyWord) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().contains(keyWord) ||
                        item.getDescription().contains(keyWord))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isItemExists(long itemId) {
        return items.containsKey(itemId);
    }
}
