package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.UserIsNotBookerException;
import ru.practicum.shareit.item.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = ensureUserExists(userId);

        Item item = ItemMapper.toItem(itemDto, owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.getReferenceById(itemDto.getRequestId());
            item.setRequest(itemRequest);
        }

        Item created = itemRepository.save(item);
        log.info("предмет создан; id: {}", created.getId());

        return ItemMapper.toItemDto(created);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemUpdateDto) {
        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));

        User owner = ensureUserExists(userId);
        ensureUserIsOwner(owner, oldItem);

        itemUpdateDto.setId(itemId);

        Item itemToUpdate = ItemMapper.toItem(itemUpdateDto, owner);
        itemToUpdate = mapItemWithNullFields(oldItem, itemToUpdate);

        Item updated = itemRepository.save(itemToUpdate);

        log.info("предмет с id {} обновлен", updated.getId());
        return ItemMapper.toItemDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoWithBooking getItemById(Long userId, Long itemId) {
        ensureUserExists(userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));
        log.info("найден предмет с id {}", item.getId());

        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        log.info("найденные комментарии к вещи: {}", comments);

        if (!item.getOwner().getId().equals(userId)) {
            log.info("пользователь {} не является владельцем предмета {}", userId, item);
            return ItemMapper.toItemDtoWithBooking(item, comments);
        }

        BookingStatus status = BookingStatus.APPROVED;
        List<Booking> bookings = bookingRepository.findAllByItemAndStatusOrderByStartDateAsc(item, status);
        log.info("найденные бронирования для вещи {} со статусом {}: {}", item, status, bookings);

        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = getLastBooking(bookings, now);
        log.info("последнее бронироварие: {}", lastBooking);
        Booking nextBooking = getNextBooking(bookings, now);
        log.info("следующее бронирование: {}", nextBooking);

        return ItemMapper.toItemDtoWithBooking(item, comments, lastBooking, nextBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDtoWithBooking> getAllUserItems(Long userId) {
        ensureUserExists(userId);

        List<Item> ownerItems = new ArrayList<>(itemRepository.findAllByOwnerId(userId));
        log.info("найденные предметы пользователя: {}", ownerItems);

        List<ItemDtoWithBooking> dtos = new ArrayList<>();
        for (Item item : ownerItems) {
            Long itemId = item.getId();

            List<Comment> comments = commentRepository.findAllByItemId(itemId);
            log.info("найденные комментарии к вещи: {}", comments);

            BookingStatus status = BookingStatus.APPROVED;
            List<Booking> bookings = bookingRepository.findAllByItemAndStatusOrderByStartDateAsc(item, status);
            log.info("найденные бронирования для вещи {} со статусом {}: {}", item, status, bookings);

            LocalDateTime now = LocalDateTime.now();
            Booking lastBooking = getLastBooking(bookings, now);
            log.info("последнее бронироварие: {}", lastBooking);
            Booking nextBooking = getNextBooking(bookings, now);
            log.info("следующее бронирование: {}", nextBooking);

            dtos.add(ItemMapper.toItemDtoWithBooking(item, comments, lastBooking, nextBooking));
        }

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> getAvailableItemsByKeyWord(Long userId, String keyWord) {
        ensureUserExists(userId);

        log.info("найдены предметы пользователя {} по ключевому слову {}", userId, keyWord);
        return itemRepository.findAllByAvailableTrueAndDescriptionContainingIgnoreCase(keyWord).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDtoResponse addComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));

        List<Booking> userBookings = bookingRepository.findAllByUserBookings(userId, itemId, LocalDateTime.now());
        if (userBookings.isEmpty()) {
            String message = "Пользователь с id {" + userId + "} не является владельцем предмета с id {" + itemId + "}";
            log.error(message);
            throw new UserIsNotBookerException(message);
        }

        Comment comment = CommentMapper.toComment(commentDto, item, user);

        Comment saved = commentRepository.save(comment);

        return CommentMapper.toCommentResponse(saved);
    }

    private User ensureUserExists(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            String message = "пользователь с id " + userId + " не существует";
            log.error(message);
            throw new UserNotFoundException(message);
        }

        log.info("пользователь с id {} найден", userId);
        return userOpt.get();
    }

    private void ensureUserIsOwner(final User user, final Item item) {
        final Long itemOwnerId = item.getOwner().getId();
        final Long userId = user.getId();

        if (!itemOwnerId.equals(userId)) {
            String message = "пользователь с id " + userId +
                    " не является владельцем предмета с id " + item.getId();
            log.error(message);
            throw new UserIsNotOwnerException(message);
        }

        log.info("пользователь с id {} является владельцем предмета с id {}", userId, item.getId());
    }

    private Item mapItemWithNullFields(final Item oldItem, final Item itemToUpdate) {
        return Item.builder()
                .id(oldItem.getId())
                .name(itemToUpdate.getName() != null ? itemToUpdate.getName() : oldItem.getName())
                .description(itemToUpdate.getDescription() != null ? itemToUpdate.getDescription() : oldItem.getDescription())
                .available(itemToUpdate.getAvailable() != null ? itemToUpdate.getAvailable() : oldItem.getAvailable())
                .owner(itemToUpdate.getOwner() != null ? itemToUpdate.getOwner() : oldItem.getOwner())
                .build();
    }

    private ItemNotFoundException generateItemNotFoundException(long itemId) {
        String message = "предмет с id " + itemId + " не существует";
        log.error(message);
        return new ItemNotFoundException(message);
    }

    private UserNotFoundException generateUserNotFoundException(long userId) {
        String message = "пользователь с id " + userId + " не существует";
        log.error(message);
        return new UserNotFoundException(message);
    }

    private Booking getLastBooking(List<Booking> bookings, LocalDateTime time) {
        return bookings
                .stream()
                .filter(booking -> !booking.getStartDate().isAfter(time))
                .reduce((booking1, booking2) -> booking1.getStartDate().isAfter(booking2.getStartDate()) ? booking1 : booking2)
                .orElse(null);
    }

    private Booking getNextBooking(List<Booking> bookings, LocalDateTime time) {
        return bookings
                .stream()
                .filter(booking -> booking.getStartDate().isAfter(time))
                .findFirst()
                .orElse(null);
    }
}
