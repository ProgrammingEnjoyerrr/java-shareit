package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
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
    void updateItem_whenItemNotExists_throwsItemNotFoundException() {
    }

    @Test
    void updateItem_whenUserIsNotOwner_throwsUserIsNotOwnerException() {
    }

    @Test
    void getItemById() {
    }

    @Test
    void getAllUserItems() {
    }

    @Test
    void getAvailableItemsByKeyWord() {
    }

    @Test
    void addComment() {
    }
}