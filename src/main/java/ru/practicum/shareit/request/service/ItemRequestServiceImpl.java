package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.GetItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

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

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setUserId(userId);
        itemRequest.setDescription(itemRequestRequestDto.getDescription());
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest created = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemResponseDto(created);
    }

    @Override
    public Collection<GetItemRequestResponseDto> getUserItemRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        Collection<ItemRequest> itemRequests = itemRequestRepository
                .findAllByUserIdOrderByCreatedDesc(userId);



        return null;
    }

    @Override
    public ItemRequestResponseDto getItemRequestFromOtherUsers(Long userId, Optional<Long> fromOpt, Optional<Long> sizeOpt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        return null;
    }

    @Override
    public ItemRequestResponseDto getItemRequestById(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> generateUserNotFoundException(userId));

        return null;
    }

    private UserNotFoundException generateUserNotFoundException(long userId) {
        String message = "пользователь с id " + userId + " не существует";
        log.error(message);
        return new UserNotFoundException(message);
    }
}
