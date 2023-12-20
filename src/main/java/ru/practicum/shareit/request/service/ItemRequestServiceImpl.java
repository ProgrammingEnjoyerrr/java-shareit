package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto createItemRequest(Long userId, ItemRequestRequestDto itemRequestRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestRequestDto, user);

        ItemRequest created = itemRequestRepository.save(itemRequest);
        log.info("запрос вещи добавлен: {}", created);

        return ItemRequestMapper.toItemRequestResponseDto(created);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemRequestResponseDto> getUserItemRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        Collection<ItemRequest> itemRequests = itemRequestRepository
                .findAllByRequesterId(userId);
        log.info("список запросов вещей пользователя {} : {}", user, itemRequests);

        return itemRequests.stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponseDto> getItemRequestFromOtherUsers(Long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        List<ItemRequest> itemRequests = itemRequestRepository
                .findAllByRequester_IdNotOrderByCreatedDesc(userId, PageRequest.of(from / size, size));
        log.info("список запросов вещей от других пользователей: {}", itemRequests);

        return itemRequests.stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestResponseDto getItemRequestById(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> generateItemRequestNotFoundException(requestId));
        log.info("запрос вещи {} по id {}", itemRequest, requestId);

        return ItemRequestMapper.toItemRequestResponseDto(itemRequest);
    }

    private UserNotFoundException generateUserNotFoundException(long userId) {
        String message = "пользователь с id " + userId + " не существует";
        log.error(message);
        return new UserNotFoundException(message);
    }

    private ItemRequestNotFoundException generateItemRequestNotFoundException(long requestId) {
        final String message = String.format("Запрос вещи с id = %d не был найден.", requestId);
        log.error(message);
        throw new ItemRequestNotFoundException(message);
    }
}
