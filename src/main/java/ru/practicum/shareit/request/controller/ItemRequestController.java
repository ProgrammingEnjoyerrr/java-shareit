package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.GetItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final String USER_ID_HEADER_LOG_PLACEHOLDER =
            USER_ID_HEADER + ": {}";

    private final ItemRequestService itemRequestService;

    @PostMapping
    ItemRequestResponseDto createItemRequest(@RequestHeader(value = USER_ID_HEADER) Long userId,
                                             @RequestBody @Valid ItemRequestRequestDto itemRequestRequestDto) {
        log.info("got request POST /requests");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, userId);
        log.info("request body: {}", itemRequestRequestDto);
        return itemRequestService.createItemRequest(userId, itemRequestRequestDto);
    }

    @GetMapping
    Collection<GetItemRequestResponseDto> getUserItemRequests(@RequestHeader(value = USER_ID_HEADER) Long userId) {
        log.info("got request GET /requests");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, userId);
        return itemRequestService.getUserItemRequests(userId);
    }

    @GetMapping(value = "/all")
    ItemRequestResponseDto getItemRequestFromOtherUsers(@RequestHeader(value = USER_ID_HEADER) Long userId,
                                                        @RequestParam Optional<Long> fromOpt, @RequestParam Optional<Long> sizeOpt) {
        log.info("got request GET /requests/all");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, userId);
        fromOpt.ifPresent(from -> log.info("from = {}", from));
        sizeOpt.ifPresent(size -> log.info("size = {}", size));
        return itemRequestService.getItemRequestFromOtherUsers(userId, fromOpt, sizeOpt);
    }

    @GetMapping
    ItemRequestResponseDto getItemRequestById(@RequestHeader(value = USER_ID_HEADER) Long userId,
                                              @PathVariable("requestId") Long requestId) {
        log.info("got request GET /requests/{requestId = {}}", requestId);
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, userId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}
