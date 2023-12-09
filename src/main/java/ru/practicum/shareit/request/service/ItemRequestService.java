package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.GetItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.Collection;
import java.util.Optional;

public interface ItemRequestService {
    ItemRequestResponseDto createItemRequest(Long userId, ItemRequestRequestDto itemRequestRequestDto);

    Collection<GetItemRequestResponseDto> getUserItemRequests(Long userId);

    ItemRequestResponseDto getItemRequestFromOtherUsers(Long userId, Optional<Long> fromOpt, Optional<Long> sizeOpt);

    ItemRequestResponseDto getItemRequestById(Long userId, Long requestId);
}
