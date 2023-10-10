package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingCreateResponseDto;
import ru.practicum.shareit.booking.exception.ItemIsUnavailableException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;

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
}
