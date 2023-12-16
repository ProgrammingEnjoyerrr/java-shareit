package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingCreateResponseDto;
import ru.practicum.shareit.booking.exception.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingCreateResponseDto addBooking(Long userId, BookingCreateRequestDto bookingCreateRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        long itemId = bookingCreateRequestDto.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));

        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new ItemIsUnavailableException("предмет с id " + itemId + " не доступен");
        }

        User owner = item.getOwner();
        Long ownerId = owner.getId();
        if (userId.equals(ownerId)) {
            String message = "Пользователь с id = {" + userId + "} и так является владельцем предместа с id = {" + itemId + "}";
            log.error(message);
            throw new UserIsOwnerException(message);
        }

        BookingDatesValidator.validate(bookingCreateRequestDto);

        Booking booking = new Booking();
        booking.setStartDate(bookingCreateRequestDto.getStart());
        booking.setEndDate(bookingCreateRequestDto.getEnd());
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        Booking saved = bookingRepository.save(booking);

        BookingCreateResponseDto response = toResponseDto(saved);

        return response;
    }

    @Override
    @Transactional
    public BookingCreateResponseDto refineBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> generateBookingNotFoundException(bookingId));
        BookingStatus currentStatus = booking.getStatus();
        if (currentStatus.equals(BookingStatus.APPROVED) || currentStatus.equals(BookingStatus.REJECTED)) {
            String message = "Бронирование уже имеет статус " + currentStatus;
            log.error(message);
            throw new BookingAlreadyRefinedException(message);
        }

        Item item = booking.getItem();

        User owner = item.getOwner();
        Long ownerId = owner.getId();
        if (!userId.equals(ownerId)) {
            String message = "Подтверждение или отклонение запроса на бронирование может быть выполнено только владельцем вещи; " +
                    "userId{" + userId + "} != bookerId{" + ownerId + "}";
            log.error(message);
            throw new UserIsNotOwnerException(message);
        }

        BookingStatus newStatus = Boolean.TRUE.equals(approved)
                ? BookingStatus.APPROVED
                : BookingStatus.REJECTED;
        booking.setStatus(newStatus);

        Booking updated = bookingRepository.save(booking);

        BookingCreateResponseDto response = toResponseDto(updated);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingCreateResponseDto getBookingData(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> generateBookingNotFoundException(bookingId));

        User booker = booking.getBooker();
        Long bookerId = booker.getId();

        Item item = booking.getItem();
        User owner = item.getOwner();
        Long ownerId = owner.getId();
        log.info("userId = {}, bookerId = {}, ownerId = {}", userId, bookerId, ownerId);
        if (!userId.equals(bookerId) && (!userId.equals(ownerId))) {
            String message = "Получение данных о конкретном бронировании может быть выполнено " +
                    "либо автором бронирования, либо владельцем вещи, к которой относится бронирование";
            log.error(message);
            throw new ForbiddenAccessException(message);
        }

        BookingCreateResponseDto response = toResponseDto(booking);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingCreateResponseDto> findAllBookingsForBooker(Long bookerId, String stateStr,
                                                                         Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        BookingState state = fromString(stateStr);

        userRepository.findById(bookerId).orElseThrow(() -> generateUserNotFoundException(bookerId));

        List<Booking> bookings = bookingRepository.findAllBookingsForBookerByStatus(bookerId, pageable);
        log.info("findAllBookingsForBooker bookings = {}", bookings);

        List<BookingCreateResponseDto> response = bookings.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
        log.info("findAllBookingsForBooker response = {}", response);

        if (state.equals(BookingState.ALL)) {
            return response;
        }

        if (state.equals(BookingState.CURRENT)) {
            LocalDateTime now = LocalDateTime.now();
            return response.stream()
                    .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                    .sorted(Comparator.comparing(BookingCreateResponseDto::getId))
                    .collect(Collectors.toList());
        }

        if (state.equals(BookingState.FUTURE)) {
            LocalDateTime now = LocalDateTime.now();
            return response.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .collect(Collectors.toList());
        }

        if (state.equals(BookingState.PAST)) {
            LocalDateTime now = LocalDateTime.now();
            return response.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .collect(Collectors.toList());
        }

        if (state.equals(BookingState.WAITING)) {
            return response.stream()
                    .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                    .collect(Collectors.toList());
        }

        if (state.equals(BookingState.REJECTED)) {
            return response.stream()
                    .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                    .collect(Collectors.toList());
        }

        throw new RuntimeException("unexpected booking state " + state);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingCreateResponseDto> findAllBookingsForItemsOwner(Long ownerId, String stateStr,
                                                                             Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").descending());
        BookingState state = fromString(stateStr);

        userRepository.findById(ownerId).orElseThrow(() -> generateUserNotFoundException(ownerId));

        Collection<Item> neededItems = itemRepository.findAllByOwnerId(ownerId);
        log.info("found {} needed items: {}", neededItems.size(), neededItems);

        List<Booking> allBookings = bookingRepository.findByItemIdIn(
                neededItems.stream()
                        .map(Item::getId)
                        .collect(Collectors.toList()),
                pageable);
        log.info("found {} bookings: {}", allBookings.size(), allBookings);

        List<Booking> neededBookings = allBookings.stream().filter(b -> {
            Item item = b.getItem();
            return item.getOwner().getId().equals(ownerId);
        }).collect(Collectors.toList());

        List<BookingCreateResponseDto> response = neededBookings.stream()
                .map(this::toResponseDto)
                .sorted((o1, o2) -> o2.getStart().compareTo(o1.getStart()))
                .collect(Collectors.toList());

        if (state.equals(BookingState.ALL)) {
            return response;
        }

        if (state.equals(BookingState.CURRENT)) {
            LocalDateTime now = LocalDateTime.now();
            return response.stream()
                    .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                    .sorted(Comparator.comparing(BookingCreateResponseDto::getId))
                    .collect(Collectors.toList());
        }

        if (state.equals(BookingState.FUTURE)) {
            LocalDateTime now = LocalDateTime.now();
            return response.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .collect(Collectors.toList());
        }

        if (state.equals(BookingState.PAST)) {
            LocalDateTime now = LocalDateTime.now();
            return response.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .collect(Collectors.toList());
        }

        if (state.equals(BookingState.WAITING)) {
            return response.stream()
                    .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                    .collect(Collectors.toList());
        }

        if (state.equals(BookingState.REJECTED)) {
            return response.stream()
                    .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                    .collect(Collectors.toList());
        }

        throw new RuntimeException("unexpected booking state " + state);
    }

    private UserNotFoundException generateUserNotFoundException(long userId) {
        String message = "пользователь с id " + userId + " не существует";
        log.error(message);
        return new UserNotFoundException(message);
    }

    private ItemNotFoundException generateItemNotFoundException(long itemId) {
        String message = "предмет с id " + itemId + " не существует";
        log.error(message);
        return new ItemNotFoundException(message);
    }

    private BookingNotFoundException generateBookingNotFoundException(long bookingId) {
        String message = "бронирования с id " + bookingId + " не существует";
        log.error(message);
        return new BookingNotFoundException(message);
    }

    private BookingState fromString(final String bookingStateStr) {
        try {
            return BookingState.valueOf(bookingStateStr);
        } catch (IllegalArgumentException e) {
            String message = "Unknown state: " + bookingStateStr;
            log.error(message);
            throw new BookingStateConversionException(message);
        }
    }

    private BookingCreateResponseDto toResponseDto(final Booking booking) {
        BookingCreateResponseDto dto = new BookingCreateResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStartDate());
        dto.setEnd(booking.getEndDate());
        dto.setStatus(booking.getStatus());
        dto.setBooker(booking.getBooker());
        dto.setItem(booking.getItem());
        return dto;
    }
}
