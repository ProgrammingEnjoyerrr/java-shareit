package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingCreateResponseDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.exception.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final User user = User.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final User owner = User.builder()
            .id(2L)
            .name("username2")
            .email("email2@email.com")
            .build();

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final Item item = Item.builder()
            .id(1L)
            .name("item name")
            .description("description")
            .available(true)
            .owner(owner)
            .build();

    private final Booking booking = Booking.builder()
            .id(1L)
            .startDate(LocalDateTime.now().plusDays(1L))
            .endDate(LocalDateTime.now().plusDays(2L))
            .status(BookingStatus.APPROVED)
            .item(item)
            .booker(user)
            .build();

    private final Booking bookingWaiting = Booking.builder()
            .id(1L)
            .startDate(LocalDateTime.now().plusDays(1L))
            .endDate(LocalDateTime.now().plusDays(2L))
            .status(BookingStatus.WAITING)
            .item(item)
            .booker(user)
            .build();

    private final BookingCreateRequestDto bookingDto = BookingCreateRequestDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .build();

    @Test
    void addBooking() {
        BookingCreateResponseDto expectedBookingCreateResponseDto = BookingMapper.toBookingCreateResponseDto(BookingMapper.toBooking(user, item, bookingDto));

        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(BookingMapper.toBooking(user, item, bookingDto));

        BookingCreateResponseDto actualBookingCreateResponseDto = bookingService.addBooking(userDto.getId(), bookingDto);

        assertThat(actualBookingCreateResponseDto).isEqualTo(expectedBookingCreateResponseDto);

        verify(userRepository, times(1)).findById(userDto.getId());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any());
    }


    @Test
    void addBooking_whenStartIsBeforeNow_shouldThrowBookingDatesValidatorException() {
        final BookingCreateRequestDto bookingDtoStartIsBeforeNow = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusHours(2L))
                .end(LocalDateTime.now().minusHours(1L))
                .build();

        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatExceptionOfType(BookingDatesValidatorException.class)
                .isThrownBy(() -> bookingService.addBooking(userDto.getId(), bookingDtoStartIsBeforeNow))
                .withMessage("дата начала не может находиться в прошлом");

        verify(userRepository, times(1)).findById(userDto.getId());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void addBooking_whenEndIsBeforeNow_shouldThrowBookingDatesValidatorException() {
        final BookingCreateRequestDto bookingDtoEndBeforeStart = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1L))
                .end(LocalDateTime.now().minusDays(1L))
                .build();

        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatExceptionOfType(BookingDatesValidatorException.class)
                .isThrownBy(() -> bookingService.addBooking(userDto.getId(), bookingDtoEndBeforeStart))
                .withMessage("дата окончания не может быть в прошлом");

        verify(userRepository, times(1)).findById(userDto.getId());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void addBooking_whenEndIsBeforeStart_shouldThrowBookingDatesValidatorException() {
        final BookingCreateRequestDto bookingDtoEndBeforeStart = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2L))
                .end(LocalDateTime.now().plusDays(1L))
                .build();

        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatExceptionOfType(BookingDatesValidatorException.class)
                .isThrownBy(() -> bookingService.addBooking(userDto.getId(), bookingDtoEndBeforeStart))
                .withMessage("дата окончания не может быть перед датой начала");

        verify(userRepository, times(1)).findById(userDto.getId());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void addBooking_whenEndIsEqualToStart_shouldThrowBookingDatesValidatorException() {
        LocalDateTime sameTime = LocalDateTime.now().plusDays(2L);

        final BookingCreateRequestDto bookingDtoEndBeforeStart = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(sameTime)
                .end(sameTime)
                .build();

        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatExceptionOfType(BookingDatesValidatorException.class)
                .isThrownBy(() -> bookingService.addBooking(userDto.getId(), bookingDtoEndBeforeStart))
                .withMessage("дата окончания должна отличаться от даты начала");

        verify(userRepository, times(1)).findById(userDto.getId());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void addBooking_whenItemIsNotAvailable_shouldThrowItemIsUnavailableException() {
        item.setAvailable(false);
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatExceptionOfType(ItemIsUnavailableException.class)
                .isThrownBy(() -> bookingService.addBooking(userDto.getId(), bookingDto))
                .withMessage("предмет с id " + item.getId() + " не доступен");

        verify(userRepository, times(1)).findById(userDto.getId());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void addBooking_whenUserDoesntExist_shouldThrowUserNotFoundException() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.empty());

        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> bookingService.addBooking(userDto.getId(), bookingDto))
                .withMessage("пользователь с id " + item.getId() + " не существует");

        verify(userRepository, times(1)).findById(userDto.getId());
    }

    @Test
    void addBooking_whenItemDoesntExist_shouldThrowItemNotFoundException() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatExceptionOfType(ItemNotFoundException.class)
                .isThrownBy(() -> bookingService.addBooking(userDto.getId(), bookingDto))
                .withMessage("предмет с id " + item.getId() + " не существует");

        verify(userRepository, times(1)).findById(userDto.getId());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void addBooking_whenItemOwnerEqualsBooker_shouldThrowUserIsOwnerException() {
        item.setOwner(user);
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatExceptionOfType(UserIsOwnerException.class)
                .isThrownBy(() -> bookingService.addBooking(userDto.getId(), bookingDto))
                .withMessage("Пользователь с id = {" + user.getId() + "} и так является владельцем предместа с id = {" + item.getId() + "}");

        verify(userRepository, times(1)).findById(userDto.getId());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void refineBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingWaiting));
        when(bookingRepository.save(any())).thenReturn(bookingWaiting);

        BookingCreateResponseDto actualBookingCreateResponseDto = bookingService.refineBooking(owner.getId(), bookingWaiting.getId(), true);

        assertThat(actualBookingCreateResponseDto.getStatus()).isEqualTo(BookingStatus.APPROVED);

        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void refineBooking_WhenStatusNotApproved() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingWaiting));
        when(bookingRepository.save(any())).thenReturn(bookingWaiting);

        BookingCreateResponseDto actualBookingCreateResponseDto = bookingService.refineBooking(owner.getId(), bookingWaiting.getId(), false);

        assertThat(actualBookingCreateResponseDto.getStatus()).isEqualTo(BookingStatus.REJECTED);

        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void refineBooking_ShouldThrowBookingAlreadyRefinedException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThatExceptionOfType(BookingAlreadyRefinedException.class)
                .isThrownBy(() -> bookingService.refineBooking(owner.getId(), booking.getId(), false))
                .withMessage("Бронирование уже имеет статус " + booking.getStatus());

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void refineBooking_WhenUserIsNotItemOwner_ShouldThrowUserIsNotOwnerException() {
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThatExceptionOfType(UserIsNotOwnerException.class)
                .isThrownBy(() -> bookingService.refineBooking(userDto.getId(), booking.getId(), true))
                .withMessage("Подтверждение или отклонение запроса на бронирование может быть выполнено" +
                        " только владельцем вещи; userId{" + user.getId() + "} != bookerId{" + booking.getItem().getOwner().getId() + "}");

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void getBookingData() {
        BookingCreateResponseDto expectedBookingCreateResponseDto = BookingMapper.toBookingCreateResponseDto(booking);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingCreateResponseDto actualBookingCreateResponseDto = bookingService.getBookingData(user.getId(), booking.getId());

        assertThat(actualBookingCreateResponseDto).isEqualTo(expectedBookingCreateResponseDto);

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void getBookingData_WhenBookingIdIsNotValid_ShouldThrowBookingNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatExceptionOfType(BookingNotFoundException.class)
                .isThrownBy(() -> bookingService.getBookingData(1L, booking.getId()))
                .withMessage("бронирования с id " + booking.getId() + " не существует");

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void getBookingData_WhenUserIsNotItemOwner_ShouldThrowForbiddenAccessException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThatExceptionOfType(ForbiddenAccessException.class)
                .isThrownBy(() -> bookingService.getBookingData(3L, booking.getId()))
                .withMessage("Получение данных о конкретном бронировании может быть выполнено либо автором бронирования," +
                        " либо владельцем вещи, к которой относится бронирование");

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void findAllBookingsForBooker_WhenBookingStateAll() {
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllBookingsForBookerByStatus(anyLong(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForBooker(user.getId(), "ALL", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findAllBookingsForBookerByStatus(anyLong(), any());
    }

    @Test
    void findAllBookingsForBooker_WhenBookingStateCURRENT() {
        booking.setStartDate(LocalDateTime.now().minusHours(1));
        booking.setEndDate(LocalDateTime.now().plusHours(1));
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllBookingsForBookerByStatus(anyLong(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForBooker(user.getId(), "CURRENT", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findAllBookingsForBookerByStatus(anyLong(), any());
    }

    @Test
    void findAllBookingsForBooker_WhenBookingStateFUTURE() {
        booking.setStartDate(LocalDateTime.now().plusHours(1));
        booking.setEndDate(LocalDateTime.now().plusHours(2));
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllBookingsForBookerByStatus(anyLong(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForBooker(user.getId(), "FUTURE", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findAllBookingsForBookerByStatus(anyLong(), any());
    }

    @Test
    void findAllBookingsForBooker_WhenBookingStatePAST() {
        booking.setStartDate(LocalDateTime.now().minusHours(3));
        booking.setEndDate(LocalDateTime.now().minusHours(2));
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllBookingsForBookerByStatus(anyLong(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForBooker(user.getId(), "PAST", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findAllBookingsForBookerByStatus(anyLong(), any());
    }

    @Test
    void findAllBookingsForBooker_WhenBookingStateWAITING() {
        booking.setStatus(BookingStatus.WAITING);
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllBookingsForBookerByStatus(anyLong(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForBooker(user.getId(), "WAITING", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findAllBookingsForBookerByStatus(anyLong(), any());
    }

    @Test
    void findAllBookingsForBooker_WhenBookingStateREJECTED() {
        booking.setStatus(BookingStatus.REJECTED);
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllBookingsForBookerByStatus(anyLong(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForBooker(user.getId(), "REJECTED", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findAllBookingsForBookerByStatus(anyLong(), any());
    }

    @Test
    void findAllBookingsForBooker_WhenBookingStateIsNotValid_ShouldThrowBookingStateConversionException() {
        assertThatExceptionOfType(BookingStateConversionException.class)
                .isThrownBy(() -> bookingService.findAllBookingsForBooker(user.getId(), "ERROR", 0, 10))
                .withMessage("Unknown state: ERROR");
    }

    @Test
    void findAllBookingsForItemsOwner_WhenBookingStateAll() {
        user.setId(2L);
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemIdIn(any(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForItemsOwner(user.getId(), "ALL", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findByItemIdIn(any(), any());
    }

    @Test
    void findAllBookingsForItemsOwner_WhenBookingStateCURRENT() {
        booking.setStartDate(LocalDateTime.now().minusHours(1));
        booking.setEndDate(LocalDateTime.now().plusHours(1));
        user.setId(2L);
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemIdIn(any(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForItemsOwner(user.getId(), "CURRENT", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findByItemIdIn(any(), any());
    }

    @Test
    void findAllBookingsForItemsOwner_WhenBookingStateFUTURE() {
        booking.setStartDate(LocalDateTime.now().plusHours(1));
        booking.setEndDate(LocalDateTime.now().plusHours(2));
        user.setId(2L);
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemIdIn(any(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForItemsOwner(user.getId(), "FUTURE", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findByItemIdIn(any(), any());
    }

    @Test
    void findAllBookingsForItemsOwner_WhenBookingStatePAST() {
        booking.setStartDate(LocalDateTime.now().minusHours(3));
        booking.setEndDate(LocalDateTime.now().minusHours(2));
        user.setId(2L);
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemIdIn(any(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForItemsOwner(user.getId(), "PAST", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findByItemIdIn(any(), any());
    }

    @Test
    void findAllBookingsForItemsOwner_WhenBookingStateWAITING() {
        booking.setStatus(BookingStatus.WAITING);
        user.setId(2L);
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemIdIn(any(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForItemsOwner(user.getId(), "WAITING", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findByItemIdIn(any(), any());
    }

    @Test
    void findAllBookingsForItemsOwner_WhenBookingStateREJECTED() {
        booking.setStatus(BookingStatus.REJECTED);
        user.setId(2L);
        List<BookingCreateResponseDto> expectedBookingsDtoOut = List.of(BookingMapper.toBookingCreateResponseDto(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemIdIn(any(), any())).thenReturn(List.of(booking));

        Collection<BookingCreateResponseDto> actualBookingsDtoOut = bookingService.findAllBookingsForItemsOwner(user.getId(), "REJECTED", 0, 10);

        assertThat(actualBookingsDtoOut).usingRecursiveAssertion().isEqualTo(expectedBookingsDtoOut);

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, times(1)).findByItemIdIn(any(), any());
    }

    @Test
    void findAllBookingsForItemsOwner_WhenBookingStateIsNotValidThenThrowIllegalArgumentException() {
        assertThatExceptionOfType(BookingStateConversionException.class)
                .isThrownBy(() -> bookingService.findAllBookingsForItemsOwner(user.getId(), "ERROR", 0, 10))
                .withMessage("Unknown state: ERROR");
    }
}