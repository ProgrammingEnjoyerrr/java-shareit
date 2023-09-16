package ru.practicum.shareit.item.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.services.ItemService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    ItemDto createItem(@RequestHeader(USER_ID_HEADER) Long userId,
                       @RequestBody @Valid ItemDto itemDto) {

        return null;
    }

    @PatchMapping(value = "/{itemId}")
    ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                       @PathVariable("itemId") Long itemId,
                       @RequestBody ItemDto itemDto) {
        return null;
    }

    @GetMapping(value = "/{itemId}")
    ItemDto getItemByUsedId(@RequestHeader(USER_ID_HEADER) Long userId,
                            @PathVariable("itemId") Long itemId) {
        return null;
    }

    @GetMapping
    Collection<ItemDto> getAllUserItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        return null;
    }

    @GetMapping(value = "/search")
    ItemDto getItemBySearch(@RequestHeader(USER_ID_HEADER) Long userId,
                            @RequestParam String text) {
        return null;
    }
}
