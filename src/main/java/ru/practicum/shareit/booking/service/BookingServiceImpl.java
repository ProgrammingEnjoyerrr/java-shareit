package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    public BookingCreateResponseDto addBooking(Long userId, BookingCreateRequestDto bookingCreateRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        long itemId = bookingCreateRequestDto.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));

        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new ItemIsUnavailableException("предмет с id " + itemId + " не доступен");
        }

        Long ownerId = item.getOwnerId();
        if (userId.equals(ownerId)) {
            String message = "Пользователь с id = {" + userId + "} и так является владельцем предместа с id = {" + itemId + "}";
            log.error(message);
            throw new UserIsOwnerException(message);
        }

        BookingDatesValidator.validate(bookingCreateRequestDto);

        Booking booking = new Booking();
        booking.setStartDate(bookingCreateRequestDto.getStart());
        booking.setEndDate(bookingCreateRequestDto.getEnd());
        booking.setBookerId(userId);
        booking.setItemId(bookingCreateRequestDto.getItemId());
        booking.setStatus(BookingStatus.WAITING);
        Booking saved = bookingRepository.save(booking);

        BookingCreateResponseDto response = new BookingCreateResponseDto();
        response.setId(saved.getId());
        response.setStart(saved.getStartDate());
        response.setEnd(saved.getEndDate());
        response.setStatus(saved.getStatus());
        response.setBooker(user);
        response.setItem(item);

        return response;
    }

    @Override
    public BookingCreateResponseDto refineBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> generateBookingNotFoundException(bookingId));
        BookingStatus currentStatus = booking.getStatus();
        if (currentStatus.equals(BookingStatus.APPROVED) || currentStatus.equals(BookingStatus.REJECTED)) {
            String message = "Бронирование уже имеет статус " + currentStatus;
            log.error(message);
            throw new BookingAlreadyRefinedException(message);
        }

        Long itemId = booking.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));
        Long ownerId = item.getOwnerId();

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

        BookingCreateResponseDto response = new BookingCreateResponseDto();
        response.setId(updated.getId());
        response.setStart(updated.getStartDate());
        response.setEnd(updated.getEndDate());
        response.setStatus(updated.getStatus());

        Long bookerId = booking.getBookerId();
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> generateUserNotFoundException(bookerId));
        response.setBooker(booker);

        response.setItem(item);

        return response;
    }

    @Override
    public BookingCreateResponseDto getBookingData(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> generateBookingNotFoundException(bookingId));

        Long bookerId = booking.getBookerId();
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> generateUserNotFoundException(bookerId));

        Long itemId = booking.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> generateItemNotFoundException(itemId));
        Long ownerId = item.getOwnerId();
        log.info("userId = {}, bookerId = {}, ownerId = {}", userId, bookerId, ownerId);
        if (!userId.equals(bookerId) && (!userId.equals(ownerId))) {
            String message = "Получение данных о конкретном бронировании может быть выполнено " +
                    "либо автором бронирования, либо владельцем вещи, к которой относится бронирование";
            log.error(message);
            throw new ForbiddenAccessException(message);
        }

        BookingCreateResponseDto response = new BookingCreateResponseDto();
        response.setId(booking.getId());
        response.setStart(booking.getStartDate());
        response.setEnd(booking.getEndDate());
        response.setStatus(booking.getStatus());
        response.setBooker(booker);
        response.setItem(item);

        return response;
    }

    @Override
    public Collection<BookingCreateResponseDto> findAllBookingsForBooker(Long bookerId, String stateStr) {
        BookingState state = fromString(stateStr);

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> generateUserNotFoundException(bookerId));

        List<Booking> bookings = bookingRepository.findAllBookingsForBookerByStatus(bookerId);
        log.info("findAllBookingsForBooker bookings = {}", bookings);

        List<BookingCreateResponseDto> response = bookings.stream()
                .map(booking -> {
                    BookingCreateResponseDto dto = new BookingCreateResponseDto();
                    dto.setId(booking.getId());
                    dto.setStart(booking.getStartDate());
                    dto.setEnd(booking.getEndDate());
                    dto.setStatus(booking.getStatus());
                    dto.setBooker(booker);

                    Long itemId = booking.getItemId();
                    Item item = itemRepository.findById(itemId)
                            .orElseThrow(() -> generateItemNotFoundException(itemId));
                    dto.setItem(item);
                    return dto;
                })
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

        throw new RuntimeException("unexpected status CURRENT");
    }

    @Override
    public Collection<BookingCreateResponseDto> findAllBookingsForItemsOwner(Long ownerId, String stateStr) {
        BookingState state = fromString(stateStr);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> generateUserNotFoundException(ownerId));

        List<Booking> allBookings = bookingRepository.findAll();

        List<Booking> neededBookings = allBookings.stream().filter(b -> {
            Long itemId = b.getItemId();
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> generateItemNotFoundException(itemId));
            return item.getOwnerId().equals(owner.getId());
        }).collect(Collectors.toList());

        List<BookingCreateResponseDto> response = neededBookings.stream()
                .map(booking -> {
                    BookingCreateResponseDto dto = new BookingCreateResponseDto();
                    dto.setId(booking.getId());
                    dto.setStart(booking.getStartDate());
                    dto.setEnd(booking.getEndDate());
                    dto.setStatus(booking.getStatus());

                    Long bookerId = booking.getBookerId();
                    User booker = userRepository.findById(bookerId)
                            .orElseThrow(() -> generateUserNotFoundException(bookerId));
                    dto.setBooker(booker);

                    Long itemId = booking.getItemId();
                    Item item = itemRepository.findById(itemId)
                            .orElseThrow(() -> generateItemNotFoundException(itemId));
                    dto.setItem(item);
                    return dto;
                }).sorted((o1, o2) -> o2.getStart().compareTo(o1.getStart()))
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

        throw new RuntimeException("unexpected status CURRENT");
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
        String message = "бронирование с id " + bookingId + " не существует";
        log.error(message);
        return new BookingNotFoundException(message);
    }

    private BookingState fromString(final String bookingStateStr) {
        try {
            return BookingState.valueOf(bookingStateStr);
        } catch (IllegalArgumentException e) {
            String message = "Unknown state: UNSUPPORTED_STATUS";
            log.error(message);
            throw new BookingStateConversionException(message);
        }
    }
}
