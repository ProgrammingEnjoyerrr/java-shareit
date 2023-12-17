package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.exception.*;
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

        List<Booking> allBookings = bookingRepository.findAll();
        log.info("all bookings: {}", allBookings);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByItemId(itemId, Sort.by(Sort.Direction.DESC, "startDate"))
                .stream().filter(b -> b.getStatus().equals(BookingStatus.APPROVED))
                .collect(Collectors.toList());
        log.info("found bookings: {}", bookings);
        List<Item> itemsOfOwner = new ArrayList<>(itemRepository.findAllByOwnerId(userId));
        log.info("found items of owner: {}", itemsOfOwner);

        ItemDtoWithBooking dto = new ItemDtoWithBooking();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        List<CommentDtoResponse> cmts = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDtoResponse commentDto = new CommentDtoResponse();
            commentDto.setId(comment.getId());
            commentDto.setText(comment.getText());
            commentDto.setAuthorName(comment.getAuthor().getName());
            commentDto.setCreated(comment.getCreated());
            cmts.add(commentDto);
        }
        dto.setComments(cmts);

        Optional<Item> foundItem = itemsOfOwner.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst();
        if (foundItem.isEmpty()) {
            log.info("user {} is not owner of item {}", userId, itemId);
            return dto;
        }

        if (!bookings.isEmpty()) {
            List<Booking> bookingsBeforeNow = bookings.stream()
                    .filter(b -> b.getStartDate().isBefore(now))
                    .collect(Collectors.toList());
            if (bookingsBeforeNow.isEmpty()) {
                dto.setLastBooking(null);
            } else {
                Booking lastBooking = bookingsBeforeNow.get(0);
                log.info("lastBooking = {}", lastBooking);
                dto.setLastBooking(new ItemDtoWithBooking.BookingMetaData(
                        lastBooking.getId(), lastBooking.getBooker().getId()));
                if (bookings.size() == 1) {
                    log.info("only 1 booking");
                    return dto;
                }
            }

            List<Booking> afterNow = bookings.stream()
                    .filter(b -> b.getStartDate().isAfter(now))
                    .collect(Collectors.toList());
            log.info("afterNow = {}", afterNow);

            Booking nextBooking = afterNow.get(afterNow.size() - 1);
            log.info("nextBooking = {}", nextBooking);
            dto.setNextBooking(new ItemDtoWithBooking.BookingMetaData(
                    nextBooking.getId(), nextBooking.getBooker().getId()));
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDtoWithBooking> getAllUserItems(Long userId) {
        ensureUserExists(userId);

        List<Item> itemsOfOwner = new ArrayList<>(itemRepository.findAllByOwnerId(userId));
        log.info("found items of owner: {}", itemsOfOwner);

        List<ItemDtoWithBooking> dtos = new ArrayList<>();

        for (Item item : itemsOfOwner) {
            ItemDtoWithBooking dto = new ItemDtoWithBooking();
            Long itemId = item.getId();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setAvailable(item.getAvailable());

            LocalDateTime now = LocalDateTime.now();
            List<Booking> bookings = bookingRepository.findByItemId(itemId, Sort.by(Sort.Direction.DESC, "startDate"))
                    .stream().filter(b -> b.getStatus().equals(BookingStatus.APPROVED))
                    .collect(Collectors.toList());
            log.info("found bookings for item {}: {}", item, bookings);

            if (bookings.isEmpty()) {
                dtos.add(dto);
            }

            if (!bookings.isEmpty()) {
                Booking lastBooking = bookings.get(bookings.size() - 1);
                log.info("lastBooking = {}", lastBooking);
                dto.setLastBooking(new ItemDtoWithBooking.BookingMetaData(
                        lastBooking.getId(), lastBooking.getBooker().getId()));
                if (bookings.size() == 1) {
                    log.info("only 1 booking");
                    dtos.add(dto);
                    continue;
                }

                List<Booking> afterNow = bookings.stream()
                        .filter(b -> b.getStartDate().isAfter(now))
                        .collect(Collectors.toList());
                log.info("afterNow = {}", afterNow);

                Booking nextBooking = afterNow.get(afterNow.size() - 1);
                log.info("nextBooking = {}", nextBooking);
                dto.setNextBooking(new ItemDtoWithBooking.BookingMetaData(
                        nextBooking.getId(), nextBooking.getBooker().getId()));
                dtos.add(dto);
            }
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

        // check that user booked item
        List<Booking> userBookings = bookingRepository.findByBookerId(userId);
        Optional<Booking> bookingOpt = userBookings.stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .findFirst();
        if (bookingOpt.isEmpty()) {
            String message = "Пользователь с id {" + userId + "} не является владельцем предмета с id {" + itemId + "}";
            log.error(message);
            throw new UserIsNotBookerException(message);
        }

        Booking booking = bookingOpt.get();
        if (!booking.getStatus().equals(BookingStatus.APPROVED)) {
            String message = "Бронирование с id {" + booking.getId() + "} не подтверждено. Текущий статус: {" + booking.getStatus() + "}";
            log.error(message);
            throw new BookingNotApprovedException(message);
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getStartDate().isAfter(now)) {
            String message = "Невозможно добавить комментарий к бронированию, которое находится в будущем. " +
                    "Текущее время {" + now + "}, время начала бронирования {" + booking.getStartDate() + "}";
            log.error(message);
            throw new BookingInFutureException(message);
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(itemRepository.findById(itemId).orElseThrow(() -> generateItemNotFoundException(itemId)));
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        CommentDtoResponse response = new CommentDtoResponse();
        response.setId(saved.getId());
        response.setText(saved.getText());
        response.setAuthorName(user.getName());
        response.setCreated(saved.getCreated());

        return response;
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

    private void ensureItemExists(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            String message = "предмет с id " + itemId + " не существует";
            log.error(message);
            throw new ItemNotFoundException(message);
        }

        log.info("предмет с id {} найден", itemId);
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
}
