package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {

//    Collection<Item> getAvailableItemsByKeyWord(String keyWord);

    Collection<Item> findAllByOwnerId(long userId);

    // TODO сделать правильный поиск (по имени тоже)
    Collection<Item> findAllByAvailableTrueAndDescriptionContainingIgnoreCase(String keyWord);

}
