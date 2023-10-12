package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        ensureUserExists(userId);

        Item item = ItemMapper.toItem(itemDto, userId);

        Item created = itemRepository.save(item);

        log.info("предмет создан; id: {}", created.getId());
        return ItemMapper.toItemDto(created);
    }

    @Override
    public ItemUpdateDto updateItem(Long userId, Long itemId, ItemUpdateDto itemUpdateDto) {
        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));

        ensureUserExists(userId);
        ensureUserIsOwner(userId, itemId);

        itemUpdateDto.setId(itemId);

        Item itemToUpdate = ItemMapper.toItem(itemUpdateDto);
        itemToUpdate = mapItemWithNullFields(oldItem, itemToUpdate);

        Item updated = itemRepository.save(itemToUpdate);

        log.info("предмет с id {} обновлен", updated.getId());
        return ItemMapper.toItemUpdateDto(updated);
    }

    @Override
    public ItemWithBookingDto getItemById(Long userId, Long itemId) {
        ensureUserExists(userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));
        log.info("найден предмет с id {}", item.getId());

        List<Booking> allBookings = bookingRepository.findAll();
        log.info("all bookings: {}", allBookings);

        LocalDateTime now = LocalDateTime.now();
        //List<Booking> bookings = bookingRepository.findByBookerIdAndEndDateIsBefore(userId, now, Sort.by(Sort.Direction.DESC, "startDate"));
        //List<Booking> bookings = bookingRepository.findByBookerId(userId, Sort.by(Sort.Direction.DESC, "startDate"));
        List<Booking> bookings = bookingRepository.findByItemId(itemId, Sort.by(Sort.Direction.DESC, "startDate"));
        log.info("found bookings: {}", bookings);

        ItemWithBookingDto dto = new ItemWithBookingDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        if (!bookings.isEmpty()) {
            Booking lastBooking = bookings.get(0);
            log.info("lastBooking = {}", lastBooking);
            dto.setLastBooking(new ItemWithBookingDto.BookingMetaData(lastBooking.getId(), lastBooking.getBookerId()));
            if (bookings.size() == 1) {
                log.info("only 1 booking");
                return dto;
            }

            List<Booking> afterNow = bookings.stream()
                    .filter(b -> b.getStartDate().isAfter(now))
                    .collect(Collectors.toList());

            Booking nextBooking = afterNow.get(afterNow.size() - 1);
            log.info("nextBooking = {}", nextBooking);
            dto.setNextBooking(new ItemWithBookingDto.BookingMetaData(nextBooking.getId(), nextBooking.getBookerId()));
        }

        return dto;
    }

    @Override
    public Collection<ItemDto> getAllUserItems(Long userId) {
        ensureUserExists(userId);

        log.info("найдены все предметы пользователя {}", userId);
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> getAvailableItemsByKeyWord(Long userId, String keyWord) {
        ensureUserExists(userId);

        log.info("найдены предметы пользователя {} по ключевому слову {}", userId, keyWord);
        return itemRepository.findAllByAvailableTrueAndDescriptionContainingIgnoreCase(keyWord).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            String message = "пользователь с id " + userId + " не существует";
            log.error(message);
            throw new UserNotFoundException(message);
        }

        log.info("пользователь с id {} найден", userId);
    }

    private void ensureItemExists(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            String message = "предмет с id " + itemId + " не существует";
            log.error(message);
            throw new ItemNotFoundException(message);
        }

        log.info("предмет с id {} найден", itemId);
    }

    private void ensureUserIsOwner(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));
        if (!item.getOwnerId().equals(userId)) {
            String message = "пользователь с id " + userId +
                    " не является владельцем предмета с id " + itemId;
            log.error(message);
            throw new UserIsNotOwnerException(message);
        }

        log.info("пользователь с id {} является владельцем предмета с id {}", userId, itemId);
    }

    private Item mapItemWithNullFields(final Item oldItem, final Item itemToUpdate) {
        return Item.builder()
                .id(oldItem.getId())
                .name(itemToUpdate.getName() != null ? itemToUpdate.getName() : oldItem.getName())
                .description(itemToUpdate.getDescription() != null ? itemToUpdate.getDescription() : oldItem.getDescription())
                .available(itemToUpdate.getAvailable() != null ? itemToUpdate.getAvailable() : oldItem.getAvailable())
                .ownerId(oldItem.getOwnerId())
                .build();
    }

    private ItemNotFoundException generateItemNotFoundException(long itemId) {
        String message = "предмет с id " + itemId + " не существует";
        log.error(message);
        return new ItemNotFoundException(message);
    }
}
