package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        ensureUserExists(userId);

        Item item = ItemMapper.toItem(itemDto, userId);

        Item created = itemRepository.createItem(item);

        return ItemMapper.toItemDto(created);
    }

    @Override
    public ItemUpdateDto updateItem(Long userId, Long itemId, ItemUpdateDto itemUpdateDto) {
        ensureUserExists(userId);
        ensureItemExists(itemId);
        ensureUserIsOwner(userId, itemId);

        itemUpdateDto.setId(itemId);
        Item itemToUpdate = ItemMapper.toItem(itemUpdateDto);

        Optional<Item> updatedOpt = itemRepository.updateItem(itemToUpdate);

        return ItemMapper.toItemUpdateDto(updatedOpt.get());
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        ensureUserExists(userId);
        ensureItemExists(itemId);

        Optional<Item> itemOpt = itemRepository.getItemById(itemId);
        return ItemMapper.toItemDto(itemOpt.get());
    }

    @Override
    public Collection<ItemDto> getAllUserItems(Long userId) {
        ensureUserExists(userId);

        return itemRepository.getAllItems(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> getAvailableItemsByKeyWord(Long userId, String keyWord) {
        ensureUserExists(userId);



        return itemRepository.getAvailableItemsByKeyWord(keyWord).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.isUserExists(userId)) {
            String message = "пользователь с id " + userId + " не существует";
            log.error(message);
            throw new UserNotFoundException(message);
        }
    }

    private void ensureItemExists(Long itemId) {
        if (!itemRepository.isItemExists(itemId)) {
            String message = "предмет с id " + itemId + " не существует";
            log.error(message);
            throw new ItemNotFoundException(message);
        }
    }

    private void ensureUserIsOwner(Long userId, Long itemId) {
        Item item = itemRepository.getItemById(itemId).get();
        if (!item.getOwnerId().equals(userId)) {
            String message = "пользователь " + userId + " не является владельцем предмета " + itemId;
            log.error(message);
            throw new UserIsNotOwnerException(message);
        }
    }
}
