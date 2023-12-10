package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.Collection;
import java.util.Optional;

public interface ItemRequestService {
    ItemRequestResponseDto createItemRequest(Long userId, ItemRequestRequestDto itemRequestRequestDto);

    Collection<ItemRequestResponseDto> getUserItemRequests(Long userId);

    Collection<ItemRequestResponseDto> getItemRequestFromOtherUsers(Long userId, Integer from, Integer size);

    ItemRequestResponseDto getItemRequestById(Long userId, Long requestId);
}
