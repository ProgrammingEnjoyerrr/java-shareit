package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private final User user = User.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final User fakeUser = User.builder()
            .id(2L)
            .name("faker")
            .email("abrakadabra@email.ru")
            .build();

    private final ItemRequestRequestDto itemRequestRequestDto = ItemRequestRequestDto.builder()
            .description("description")
            .build();

    private final ItemRequest itemRequest =
            ItemRequestMapper.toItemRequest(itemRequestRequestDto, user);

    @Test
    void createItemRequest() {
        Long userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);

        ItemRequestResponseDto response = itemRequestService.createItemRequest(userId, itemRequestRequestDto);

        assertThat(response).isEqualTo(ItemRequestMapper.toItemRequestResponseDto(itemRequest));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    void getUserItemRequests() {
        Long userId = user.getId();

        Collection<ItemRequest> itemRequests = List.of(itemRequest);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(itemRequests);

        Collection<ItemRequestResponseDto> response = itemRequestService.getUserItemRequests(userId);

        assertThat(response)
                .isNotNull()
                .usingRecursiveAssertion()
                .isEqualTo(itemRequests.stream()
                        .map(ItemRequestMapper::toItemRequestResponseDto)
                        .collect(Collectors.toList()));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findAllByRequesterId(userId);
    }

    @Test
    void getItemRequestFromOtherUsers() {
        Long userId = fakeUser.getId();
        Integer from = 0;
        Integer size = 10;
        var pageable = PageRequest.of(from / size, size);

        List<ItemRequest> expectedItemRequests = List.of(itemRequest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
        when(itemRequestRepository
                .findAllByRequester_IdNotOrderByCreatedDesc(userId, pageable))
                .thenReturn(expectedItemRequests);

        List<ItemRequestResponseDto> response = itemRequestService
                .getItemRequestFromOtherUsers(userId, from, size);

        assertThat(response)
                .isNotNull()
                .usingRecursiveAssertion()
                .isEqualTo(expectedItemRequests.stream()
                        .map(ItemRequestMapper::toItemRequestResponseDto)
                        .collect(Collectors.toList()));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1))
                .findAllByRequester_IdNotOrderByCreatedDesc(userId, pageable);
    }

    @Test
    void getItemRequestById() {
        Long userId = user.getId();
        Long requestId = 1L;
        itemRequest.setId(requestId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        ItemRequestResponseDto response = itemRequestService.getItemRequestById(userId, requestId);

        assertThat(response).isEqualTo(ItemRequestMapper.toItemRequestResponseDto(itemRequest));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findById(requestId);
    }

    @Test
    void getItemRequestById_whenUserNotFound_throwsUserNotFoundException() {
        Long userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> itemRequestService.getItemRequestById(userId, anyLong()))
                .withMessage("пользователь с id " + userId + " не существует");

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getItemRequestById_whenItemRequestNotFound_throwsItemRequestNotFoundException() {
        Long userId = user.getId();
        Long requestId = 1L;
        itemRequest.setId(requestId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ItemRequestNotFoundException.class)
                .isThrownBy(() -> itemRequestService.getItemRequestById(userId, requestId))
                .withMessage("Запрос вещи с id = " + requestId + " не был найден.");

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findById(requestId);
    }
}