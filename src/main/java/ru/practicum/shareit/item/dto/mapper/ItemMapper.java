package ru.practicum.shareit.item.dto.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.BookingMetaData;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {
    public static Item toItem(ItemDto dto, User owner) {
        return Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .build();
    }

    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();

        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }

        return itemDto;
    }

    public static ItemDtoWithBooking toItemDtoWithBooking(final Item item, final List<Comment> comments) {
        ItemDtoWithBooking dto = new ItemDtoWithBooking();
        dto.setItem(item);
        List<CommentDtoResponse> commentsDto = comments.stream()
                .map(CommentMapper::toCommentResponse)
                .collect(Collectors.toList());
        dto.setComments(commentsDto);

        return dto;
    }

    public static ItemDtoWithBooking toItemDtoWithBooking(final Item item, final List<Comment> comments,
                                                          final Booking lastBooking,
                                                          final Booking nextBooking) {
        ItemDtoWithBooking dto = new ItemDtoWithBooking();

        dto.setItem(item);

        List<CommentDtoResponse> commentsDto = comments.stream()
                .map(CommentMapper::toCommentResponse)
                .collect(Collectors.toList());
        dto.setComments(commentsDto);

        dto.setLastBooking(lastBooking != null
                ? new BookingMetaData(lastBooking.getId(), lastBooking.getBooker().getId())
                : null);
        dto.setNextBooking(nextBooking != null
                ? new BookingMetaData(nextBooking.getId(), nextBooking.getBooker().getId())
                : null);

        return dto;
    }
}
