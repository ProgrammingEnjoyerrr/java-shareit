package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final String USER_ID_HEADER_LOG_PLACEHOLDER = "{}: {}";

    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader(value = USER_ID_HEADER) Long userId,
                              @RequestBody @Valid ItemDto itemDto) {
        log.info("got request POST /items");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, USER_ID_HEADER, userId);
        log.info("request body: {}", itemDto);
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping(value = "/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                                    @PathVariable("itemId") Long itemId,
                                    @RequestBody ItemDto itemUpdateDto) {
        log.info("got request PATCH /items/{itemId}");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, USER_ID_HEADER, userId);
        log.info("itemId = {}", itemId);
        log.info("request body: {}", itemUpdateDto);
        return itemService.updateItem(userId, itemId, itemUpdateDto);
    }

    @GetMapping(value = "/{itemId}")
    public ItemDtoWithBooking getItemByUserId(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @PathVariable("itemId") Long itemId) {
        log.info("got request GET /items/{itemId}");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, USER_ID_HEADER, userId);
        log.info("itemId = {}", itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public Collection<ItemDtoWithBooking> getAllUserItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("got request GET /items");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, USER_ID_HEADER, userId);
        return itemService.getAllUserItems(userId);
    }

    @GetMapping(value = "/search")
    public Collection<ItemDto> getAvailableItemsByKeyWord(@RequestHeader(USER_ID_HEADER) Long userId,
                                                          @RequestParam(name = "text") String keyWord) {
        log.info("got request GET /items/search");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, USER_ID_HEADER, userId);
        log.info("text = {}", keyWord);
        if (keyWord.isBlank()) {
            return new ArrayList<>();
        }

        return itemService.getAvailableItemsByKeyWord(userId, keyWord);
    }

    @PostMapping(value = "/{itemId}/comment")
    public CommentDtoResponse addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable("itemId") Long itemId,
                                         @RequestBody @Valid final CommentDto commentDto) {
        log.info("got request POST /items/{itemId}/comment");
        log.info(USER_ID_HEADER_LOG_PLACEHOLDER, USER_ID_HEADER, userId);
        log.info("itemId = {}", itemId);
        log.info("request body = {}", commentDto);

        return itemService.addComment(userId, itemId, commentDto);
    }
}
