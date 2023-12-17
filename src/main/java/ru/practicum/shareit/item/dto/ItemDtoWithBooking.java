package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ItemDtoWithBooking {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingMetaData lastBooking;

    private BookingMetaData nextBooking;

    private List<CommentDtoResponse> comments = new ArrayList<>();

    public void setItem(final Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.available = item.getAvailable();
    }
}
