package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private final ItemDto itemDto = ItemDto.builder()
            .id(100L)
            .name("name")
            .description("description")
            .available(true)
            .build();
    private final User owner = User.builder()
            .id(200L)
            .name("name")
            .email("example@email.com")
            .build();

    private final User user = User.builder()
            .id(2000L)
            .name("user_name")
            .email("exampleEmail@email.com")
            .build();

    private final Item item = ItemMapper.toItem(itemDto, owner);
    private final Comment comment1 = Comment.builder()
            .id(1000L)
            .text("text1")
            .item(item)
            .author(user)
            .created(LocalDateTime.now())
            .build();

    private final Comment comment2 = Comment.builder()
            .id(1001L)
            .text("text2")
            .item(item)
            .author(user)
            .created(LocalDateTime.now())
            .build();

    private final Booking booking1 = Booking.builder()
            .id(3000L)
            .startDate(LocalDateTime.now().minusHours(2))
            .endDate(LocalDateTime.now().minusHours(1))
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .build();

    private final Booking booking2 = Booking.builder()
            .id(3001L)
            .startDate(LocalDateTime.now().plusHours(1))
            .endDate(LocalDateTime.now().plusHours(2))
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .build();

    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(300L)
            .requester(owner)
            .description("description")
            .created(LocalDateTime.now())
            .items(new ArrayList<>())
            .build();

    private final ItemDto itemDtoWithRequestId = ItemDto.builder()
            .id(150L)
            .name("name")
            .description("description")
            .available(true)
            .requestId(itemRequest.getId())
            .build();

    @Test
    void createItem() {
        Item item = ItemMapper.toItem(itemDto, owner);

        when(userRepository.findById(item.getOwner().getId())).thenReturn(Optional.of(owner));
        when(itemRepository.save(item)).thenReturn(item);

        ItemDto response = itemService.createItem(owner.getId(), itemDto);

        assertThat(response).isEqualTo(itemDto);

        verify(userRepository, times(1)).findById(item.getOwner().getId());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void createItem_whenRequestIdIsNotNull() {
        Item item = ItemMapper.toItem(itemDtoWithRequestId, owner);
        item.setRequest(itemRequest);

        when(userRepository.findById(item.getOwner().getId())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.getReferenceById(itemDtoWithRequestId.getRequestId())).thenReturn(itemRequest);
        when(itemRepository.save(item)).thenReturn(item);

        ItemDto response = itemService.createItem(owner.getId(), itemDtoWithRequestId);

        assertThat(response).isEqualTo(itemDtoWithRequestId);

        verify(userRepository, times(1)).findById(item.getOwner().getId());
        verify(itemRequestRepository, times(1)).getReferenceById(itemDtoWithRequestId.getRequestId());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void createItem_whenUserNotExists_throwsUserNotFoundException() {
        Item item = ItemMapper.toItem(itemDto, owner);

        when(userRepository.findById(item.getOwner().getId())).thenReturn(Optional.empty());

        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> itemService.createItem(owner.getId(), itemDto))
                .withMessage("пользователь с id " + owner.getId() + " не существует");

        verify(userRepository, times(1)).findById(item.getOwner().getId());
    }

    @Test
    void updateItem() {
        Item item = ItemMapper.toItem(itemDto, owner);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(item.getOwner().getId())).thenReturn(Optional.of(owner));
        when(itemRepository.save(item)).thenReturn(item);

        ItemDto response = itemService.updateItem(owner.getId(), itemDto.getId(), itemDto);

        assertThat(response).isEqualTo(itemDto);

        verify(itemRepository, times(1)).findById(item.getId());
        verify(userRepository, times(1)).findById(item.getOwner().getId());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void updateItem_whenUserIsNotOwner_shouldThrowUserIsNotOwnerException() {
        Long userId = user.getId();
        Long itemId = item.getId();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatExceptionOfType(UserIsNotOwnerException.class)
                .isThrownBy(() -> itemService.updateItem(userId, itemDto.getId(), itemDto))
                .withMessage("пользователь с id " + userId + " не является владельцем предмета с id " + itemId);

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateItem_whenItemDontExists_shouldThrowItemNotFoundException() {
        Long userId = user.getId();
        Long itemId = item.getId();

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ItemNotFoundException.class)
                .isThrownBy(() -> itemService.updateItem(userId, itemDto.getId(), itemDto))
                .withMessage("предмет с id " + itemId + " не существует");

        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void getItemById() {
        Long itemId = item.getId();
        Long ownerId = item.getOwner().getId();
        List<Comment> comments = List.of(comment1, comment2);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(comments);
        when(bookingRepository.findAllByItemAndStatusOrderByStartDateAsc(item, BookingStatus.APPROVED))
                .thenReturn(List.of(booking1, booking2));

        ItemDtoWithBooking response = itemService.getItemById(ownerId, itemId);
        assertThat(response).isNotNull();

        ItemDtoWithBooking expected = ItemMapper.toItemDtoWithBooking(
                item, comments, booking1, booking2);
        assertThat(response).isEqualTo(expected);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(commentRepository, times(1)).findAllByItemId(itemId);
        verify(bookingRepository, times(1)).findAllByItemAndStatusOrderByStartDateAsc(item, BookingStatus.APPROVED);
    }

    @Test
    void getItemById_whenUserIsNotOwner_shouldReturnPartlyInfo() {
        Long itemId = item.getId();
        Long ownerId = user.getId();
        List<Comment> comments = List.of(comment1, comment2);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(comments);

        ItemDtoWithBooking response = itemService.getItemById(ownerId, itemId);
        assertThat(response).isNotNull();

        ItemDtoWithBooking expected = ItemMapper.toItemDtoWithBooking(
                item, comments);
        assertThat(response).isEqualTo(expected);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(commentRepository, times(1)).findAllByItemId(itemId);
    }

    @Test
    void getAllUserItems() {
        Long itemId = item.getId();
        Long ownerId = item.getOwner().getId();
        List<Comment> comments = List.of(comment1, comment2);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(item));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(comments);
        when(bookingRepository.findAllByItemAndStatusOrderByStartDateAsc(item, BookingStatus.APPROVED))
                .thenReturn(List.of(booking1, booking2));

        Collection<ItemDtoWithBooking> response = itemService.getAllUserItems(ownerId);
        assertThat(response).isNotNull();

        Collection<ItemDtoWithBooking> expected = List.of(ItemMapper.toItemDtoWithBooking(
                item, comments, booking1, booking2));
        assertThat(response).isEqualTo(expected);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findAllByOwnerId(ownerId);
        verify(commentRepository, times(1)).findAllByItemId(itemId);
        verify(bookingRepository, times(1)).findAllByItemAndStatusOrderByStartDateAsc(item, BookingStatus.APPROVED);
    }

    @Test
    void getAvailableItemsByKeyWord() {
        Long ownerId = item.getOwner().getId();
        String keyWord = "descr";

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByAvailableTrueAndDescriptionContainingIgnoreCase(keyWord))
                .thenReturn(List.of(item));

        Collection<ItemDto> response = itemService.getAvailableItemsByKeyWord(ownerId, keyWord);
        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(List.of(itemDto));

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1))
                .findAllByAvailableTrueAndDescriptionContainingIgnoreCase(keyWord);
    }

    @Test
    void addComment() {
        Long itemId = item.getId();
        Long userId = user.getId();
        comment1.setId(null);
        final CommentDto commentDto = CommentMapper.toCommentDto(comment1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByUserBookings(any(), any(), any())).thenReturn(List.of(booking1, booking2));
        when(commentRepository.save(any())).thenReturn(comment1);

        CommentDtoResponse response = itemService.addComment(userId, itemId, commentDto);
        assertThat(response)
                .isNotNull()
                .isEqualTo(CommentMapper.toCommentResponse(comment1));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1)).findAllByUserBookings(any(), any(), any());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void addComment_whenUserIsNotBooker_shouldThrowUserIsNotBookerException() {
        Long itemId = item.getId();
        Long userId = user.getId();
        final CommentDto commentDto = CommentMapper.toCommentDto(comment1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByUserBookings(any(), any(), any())).thenReturn(List.of());

        assertThatExceptionOfType(UserIsNotBookerException.class)
                .isThrownBy(() -> itemService.addComment(userId, itemId, commentDto))
                .withMessage("Пользователь с id {" + userId + "} не является владельцем предмета с id {" + itemId + "}");

        verify(userRepository, times(1)).findById(userId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1)).findAllByUserBookings(any(), any(), any());
    }
}